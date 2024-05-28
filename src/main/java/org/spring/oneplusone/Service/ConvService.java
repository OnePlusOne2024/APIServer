package org.spring.oneplusone.Service;

import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.Entity.ConvEntity;
import org.spring.oneplusone.Repository.ConvListRepository;
import org.spring.oneplusone.ServiceImpls.ConvCrawling;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConvService {
    private final ConvCrawling gsConvCrawling;
    private final ConvListRepository convListRepository;
    @Autowired
    private CrawlingStatus crawlingStatus;

    public ConvService(ConvCrawling gsConvCrawling, ConvListRepository convListRepository) {
        this.gsConvCrawling = gsConvCrawling;
        this.convListRepository = convListRepository;
    }

    public CrawlingResultDTO convCrawling() {
        log.debug("Service start");
        log.info("Reset DB");
        try{
            convListRepository.deleteAll();
        }catch(Exception ex){
            log.error("에러 상세 : ", ex);
            throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
        }
        //futures : 크롤링 결과가 담긴 Lis<ConvDTO>들의 CompletableFuture의 List
        List<CompletableFuture<List<ConvDTO>>> futures = new ArrayList<>();
        AtomicReference<CrawlingResultDTO> result = new AtomicReference<>();
        log.info("GS 편의점 크롤링 시작");
        futures.add(CompletableFuture.supplyAsync(() -> {
            return gsConvCrawling.getConvList().join();
        }));
        //allFutures : futures들의 작업이 끝난것의 합
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        CompletableFuture<List<ConvDTO>> allConvDTOListFutures = allFutures.thenApply(v ->
                futures.stream()
                        //CompletableFuture에서 결과 리스트를 가져옴
                        .map(CompletableFuture::join)
                        //모든 리스트를 하나의 스트림으로 평탄화
                        .flatMap(List::stream)
                        //결과 스트림을 하나의 리스트로 수집
                        .collect(Collectors.toList())
        );
        allConvDTOListFutures.thenAccept(allConvDTOList -> {
            log.info("편의점의 총 갯수: {}", allConvDTOList.size());
            List<ConvEntity> convInputData = allConvDTOList.stream()
                    .map(this::changeConvDTOToConvEntity)
                    .collect(Collectors.toList());
            try {
                convListRepository.saveAll(convInputData);
                result.set(CrawlingResultDTO.builder().resultCount(allConvDTOList.size()).build());
            } catch (Exception ex) {
                log.error("데이터 저장 중 에러 발생: {}", ex.getMessage());
                throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
            }
        }).join();
        return result.get();
    }

    public List<ConvDTO> readConvList(){
        log.debug("ConvList Service Start");
        List<ConvEntity> convEntityList = new ArrayList<>();
        try{
            convEntityList = convListRepository.findAll();
        }catch(Exception ex){
            log.error("에러 발생 : ", ex);
            throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
        }
        List<ConvDTO> result = convEntityList.stream().map(this::changeConvEntityToConvDTO).collect(Collectors.toList());
        log.debug("ConvList Service Stop");
        return result;
    }

    public List<ConvDTO> readNearConvList(double x, double y){
        log.debug("Near Conv List Service Start");
        double distanceInMeter = 1000;
        List<ConvEntity> convEntityList = new ArrayList<>();
        try{
            convEntityList = convListRepository.findNearConv(x, y, distanceInMeter);
        }catch(Exception ex){
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
                .x(convDTO.getX())
                .y(convDTO.getY())
                .convBrandName(convDTO.getConvBrandName())
                .id(UUID.randomUUID().toString())
                .build();
    }

    private ConvDTO changeConvEntityToConvDTO(ConvEntity convEntity){
        return ConvDTO.builder()
                .convBrandName(convEntity.getConvBrandName())
                .convName(convEntity.getConvName())
                .convAddr(convEntity.getConvAddr())
                .x(convEntity.getX())
                .y(convEntity.getY())
                .build();
    }
}