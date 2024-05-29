package org.spring.oneplusone.Service;

import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.Entity.ConvEntity;
import org.spring.oneplusone.Repository.ConvListRepository;
import org.spring.oneplusone.ServiceImpls.GsConvCrawling;
import org.spring.oneplusone.ServiceImpls.SevenConvCrawling;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConvService {
    private final GsConvCrawling gsConvCrawling;
    private final ConvListRepository convListRepository;
    private final SevenConvCrawling sevenConvCrawling;

    private CrawlingStatus crawlingStatus;

    public ConvService(
            CrawlingStatus crawlingStatus,
            GsConvCrawling gsConvCrawling,
            SevenConvCrawling sevenConvCrawling,
            ConvListRepository convListRepository) {
        this.gsConvCrawling = gsConvCrawling;
        this.convListRepository = convListRepository;
        this.sevenConvCrawling = sevenConvCrawling;
        this.crawlingStatus = crawlingStatus;
    }

    public CrawlingResultDTO convCrawling() {
        log.debug("Service start");
        log.info("Reset DB");
        try {
            convListRepository.deleteAll();
        } catch (Exception ex) {
            log.error("에러 상세 : ", ex);
            throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
        }
        // futures: 크롤링 결과가 담긴 List<CompletableFuture<List<ConvDTO>>>들의 List
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<List<ConvDTO>> future1 = CompletableFuture.supplyAsync(() -> sevenConvCrawling.getConvList(), executor)
                .handle((result, ex) -> {
                    if (ex != null) {
                        // future1에서 발생한 예외 처리
                        log.error("SevenEleven 편의점 크롤링 중 에러 발생(ConvService에서 처리) : {}" ,ex);
                        log.error("발생위치 : {}",ex.getStackTrace());
//                        return Collections.emptyList(); // 예외 발생 시 빈 리스트 반환
                        throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                    } else {
                        return result;
                    }
                });

        CompletableFuture<List<ConvDTO>> future2 = CompletableFuture.supplyAsync(() -> gsConvCrawling.getConvList(), executor)
                .handle((result, ex) -> {
                    if (ex != null) {
                        // future2에서 발생한 예외 처리
                        log.error("GS 편의점 크롤링 중 에러 발생(ConvService에서 처리) : {}" ,ex);
//                        return Collections.emptyList(); // 예외 발생 시 빈 리스트 반환
                        throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                    } else {
                        return result;
                    }
                });

// allFutures: futures들의 작업이 끝난 것을 합친 CompletableFuture
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2);
        // allConvDTOListFutures: 모든 futures의 결과를 하나의 리스트로 수집하는 CompletableFuture
        CompletableFuture<List<ConvDTO>> allConvDTOListFutures = allFutures.thenApply(v -> {
            List<ConvDTO> combinedList = new ArrayList<>();
            try {
                combinedList.addAll(future1.get()); // future1의 결과를 가져옴
                combinedList.addAll(future2.get()); // future2의 결과를 가져옴
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            return combinedList;
        });

        // 결과를 처리하고 저장하는 CompletableFuture
        CompletableFuture<CrawlingResultDTO> resultFuture = allConvDTOListFutures.thenApply(allConvDTOList -> {
            log.info("편의점의 총 갯수: {}", allConvDTOList.size());
            List<ConvEntity> convInputData = allConvDTOList.stream()
                    .map(this::changeConvDTOToConvEntity)
                    .collect(Collectors.toList());
            try {
                convListRepository.saveAll(convInputData);
                return CrawlingResultDTO.builder().resultCount(allConvDTOList.size()).build();
            } catch (Exception ex) {
                crawlingStatus.stopCrawling("convenienceCrawling");
                log.error("데이터 저장 중 에러 발생: {}", ex.getMessage());
                throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
            }
        });
        // 비동기 작업이 완료될 때까지 대기하고 결과 반환
        return resultFuture.join();
    }


    public List<ConvDTO> readConvList() {
        log.debug("ConvList Service Start");
        List<ConvEntity> convEntityList = new ArrayList<>();
        try {
            convEntityList = convListRepository.findAll();
        } catch (Exception ex) {
            log.error("에러 발생 : ", ex);
            throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
        }
        List<ConvDTO> result = convEntityList.stream().map(this::changeConvEntityToConvDTO).collect(Collectors.toList());
        log.debug("ConvList Service Stop");
        return result;
    }

    public List<ConvDTO> readNearConvList(double latitude, double longitude) {
        log.debug("Near Conv List Service Start");
        double distanceInMeter = 1000;
        List<ConvEntity> convEntityList = new ArrayList<>();
        try {
            convEntityList = convListRepository.findNearConv(latitude, longitude, distanceInMeter);
        } catch (Exception ex) {
            log.error("에러 디테일 : ", ex);
            throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
        }
        List<ConvDTO> result = convEntityList.stream().map(this::changeConvEntityToConvDTO).collect(Collectors.toList());
        log.debug("Near Conv List Service Finish");
        return result;
    }

    private ConvEntity changeConvDTOToConvEntity(ConvDTO convDTO) {
        return ConvEntity.builder()
                .convAddr(convDTO.getConvAddr())
                .convName(convDTO.getConvName())
                .longitude(convDTO.getLongitude())
                .latitude(convDTO.getLatitude())
                .convBrandName(convDTO.getConvBrandName())
                .id(UUID.randomUUID().toString())
                .build();
    }

    private ConvDTO changeConvEntityToConvDTO(ConvEntity convEntity) {
        return ConvDTO.builder()
                .convBrandName(convEntity.getConvBrandName())
                .convName(convEntity.getConvName())
                .convAddr(convEntity.getConvAddr())
                .longitude(convEntity.getLongitude())
                .latitude(convEntity.getLatitude())
                .build();
    }
}