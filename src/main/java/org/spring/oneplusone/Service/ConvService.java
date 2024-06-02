package org.spring.oneplusone.Service;

import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.Entity.ConvEntity;
import org.spring.oneplusone.Repository.ConvListRepository;
import org.spring.oneplusone.ServiceImpls.CuConvCrawling;
import org.spring.oneplusone.ServiceImpls.EmartConvCrawling;
import org.spring.oneplusone.ServiceImpls.GsConvCrawling;
import org.spring.oneplusone.ServiceImpls.SevenConvCrawling;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConvService {
    private final GsConvCrawling gsConvCrawling;
    private final ConvListRepository convListRepository;
    private final SevenConvCrawling sevenConvCrawling;
    private final CuConvCrawling cuConvCrawling;
    private final EmartConvCrawling emartConvCrawling;

    private CrawlingStatus crawlingStatus;

    public ConvService(
            CrawlingStatus crawlingStatus,
            GsConvCrawling gsConvCrawling,
            SevenConvCrawling sevenConvCrawling,
            CuConvCrawling cuConvCrawling,
            EmartConvCrawling emartConvCrawling,
            ConvListRepository convListRepository) {
        this.gsConvCrawling = gsConvCrawling;
        this.convListRepository = convListRepository;
        this.sevenConvCrawling = sevenConvCrawling;
        this.crawlingStatus = crawlingStatus;
        this.cuConvCrawling = cuConvCrawling;
        this.emartConvCrawling = emartConvCrawling;
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
                .exceptionally(ex -> {
                        // future1에서 발생한 예외 처리
                        log.error("SevenEleven 편의점 크롤링 중 에러 발생(ConvService에서 처리) : {}" ,ex);
                        log.error("발생위치 : {}",ex.getStackTrace());
//                        return Collections.emptyList(); // 예외 발생 시 빈 리스트 반환
                        throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        CompletableFuture<List<ConvDTO>> future2 = CompletableFuture.supplyAsync(() -> gsConvCrawling.getConvList(), executor)
                .exceptionally(ex -> {
                        // future2에서 발생한 예외 처리
                        log.error("GS 편의점 크롤링 중 에러 발생(ConvService에서 처리) : {}" ,ex);
                    log.error("발생위치 : {}",ex.getStackTrace());
                        throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        CompletableFuture<List<ConvDTO>> future3 = CompletableFuture.supplyAsync(() -> cuConvCrawling.getConvList(), executor)
                .exceptionally(ex -> {
                        // future2에서 발생한 예외 처리
                        log.error("CU 편의점 크롤링 중 에러 발생(ConvService에서 처리) : {}" ,ex);
                    log.error("발생위치 : {}",ex.getStackTrace());
                        throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        CompletableFuture<List<ConvDTO>> future4 = CompletableFuture.supplyAsync(() -> emartConvCrawling.getConvList(), executor)
                .exceptionally(ex -> {
                    log.error("Emart 편의점 크롤링 중 에러 발생(ConvService에서 처리): {}", ex);
                    log.error("발생위치 : {}",ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        // allFutures: futures들의 작업이 끝난 것을 합친 CompletableFuture
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3,
                        future4)
                .exceptionally(ex -> {
                    // CompletableFuture.allOf 자체에서 발생한 예외 처리
                    log.error("CompletableFuture.allOf 중 예외 발생: {}", ex.getMessage());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR); // 적절한 예외 처리 필요
                });
        // allConvDTOListFutures: 모든 futures의 결과를 하나의 리스트로 수집하는 CompletableFuture
        CompletableFuture<List<ConvDTO>> allConvDTOListFutures = allFutures.thenApply(v -> {
            List<ConvDTO> combinedList = new ArrayList<>();
            try {
                combinedList.addAll(future1.join()); // join()을 사용하여 예외를 unchecked 형태로 받음
                combinedList.addAll(future2.join());
                combinedList.addAll(future3.join());
                combinedList.addAll(future4.join());
            } catch (CompletionException e) {
                throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
            }
            return combinedList;
        }).exceptionally(ex -> {
            // CompletableFuture 체인의 어느 부분에서든 예외가 발생하면 여기서 처리
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
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

    public List<ConvDTO> readNearConvList(double longitude,double latitude) {
        log.debug("Near Conv List Service Start");
        double distanceInMeter = 1000;
        List<ConvEntity> convEntityList = new ArrayList<>();
        try {
            convEntityList = convListRepository.findNearConv(longitude, latitude, distanceInMeter);
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