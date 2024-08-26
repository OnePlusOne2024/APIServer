package org.spring.oneplusone.Service;


import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.DTO.SearchDTO;
import org.spring.oneplusone.Entity.*;
import org.spring.oneplusone.Repository.CrawlingTimeRepository;
import org.spring.oneplusone.Repository.ProductRepository;
import org.spring.oneplusone.Repository.SearchRepository;
import org.spring.oneplusone.ServiceImpls.Product.CU.CuEventCrawling;
import org.spring.oneplusone.ServiceImpls.Product.CU.CuPbCrawling;
import org.spring.oneplusone.ServiceImpls.Product.Emart.EmartEventCrawling;
import org.spring.oneplusone.ServiceImpls.Product.Emart.EmartPbCrawling;
import org.spring.oneplusone.ServiceImpls.Product.GS25.GsEventCrawling;
import org.spring.oneplusone.ServiceImpls.Product.GS25.GsFreshPbCrawling;
import org.spring.oneplusone.ServiceImpls.Product.GS25.GsNonFreshPbCrawling;
import org.spring.oneplusone.ServiceImpls.Product.SevenEleven.SevenEventCrawling;
import org.spring.oneplusone.ServiceImpls.Product.SevenEleven.SevenPbCrawling;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
    //Dependency Injection을 위해 생성자를 주입
    private final ProductRepository productRepository;
    private final CrawlingTimeRepository crawlingTimeRepository;
    private final GsEventCrawling gsEventCrawling;
    private final GsFreshPbCrawling gsFreshPbCrawling;
    private final GsNonFreshPbCrawling gsNonFreshPbCrawling;
    private final SevenEventCrawling sevenEventCrawling;
    private final SevenPbCrawling sevenPbCrawling;
    private final CuEventCrawling cuEventCrawling;
    private final CuPbCrawling cuPbCrawling;
    private final EmartEventCrawling emartEventCrawling;
    private final EmartPbCrawling emartPbCrawling;
    private final SearchRepository searchRepository;
    private CrawlingStatus crawlingStatus;
    private TaskScheduler taskScheduler;

    public ProductService(ProductRepository productRepository,
                          CrawlingTimeRepository crawlingTimeRepository,
                          SearchRepository searchRepository,
                          @Qualifier("taskScheduler") TaskScheduler taskScheduler,
                          SevenEventCrawling sevenEventCrawling,
                          SevenPbCrawling sevenPbCrawling,
                          CuEventCrawling cuEventCrawling,
                          CuPbCrawling cuPbCrawling,
                          EmartEventCrawling emartEventCrawling,
                          EmartPbCrawling emartPbCrawling,
                          GsFreshPbCrawling gsFreshPbCrawling,
                          GsNonFreshPbCrawling gsNonFreshPbCrawling,
                          GsEventCrawling gsEventCrawling) {
        this.productRepository = productRepository;
        this.crawlingTimeRepository = crawlingTimeRepository;
        this.gsEventCrawling = gsEventCrawling;
        this.gsFreshPbCrawling = gsFreshPbCrawling;
        this.gsNonFreshPbCrawling = gsNonFreshPbCrawling;
        this.searchRepository = searchRepository;
        this.taskScheduler = taskScheduler;
        this.sevenEventCrawling = sevenEventCrawling;
        this.sevenPbCrawling = sevenPbCrawling;
        this.cuEventCrawling = cuEventCrawling;
        this.cuPbCrawling = cuPbCrawling;
        this.emartEventCrawling = emartEventCrawling;
        this.emartPbCrawling = emartPbCrawling;
    }

    public List<ProductDTO> findAllProducts() {
        log.debug("SERVICE START");
        //repository를 통해서 DB 접속
        //read all
        List<ProductEntity> productList = productRepository.findAll();
        log.info("현재 물품 수량 : " + productList.size());
        List<ProductDTO> resultWithinDTO = productList.stream().map(this::productEntityToProductDTO).collect(Collectors.toList());
        log.debug("SERVICE FINISH");
        return resultWithinDTO;
    }

    public CrawlingResultDTO productCrawling() {
        log.debug("SERVICE START");
        //현재 진행중인 crawling이 있는지 확인하기
        //없으면 id가 1인 데이터가 있는지 확인하고, 있으면 update, 없으면 create
        //jpaRepository는 save가 자동적으로 create와 update를 한다
        crawlingTimeRepository.save(CrawlingTimeEntity.builder()
                .id(1L)
                .latestCrawlingTime(LocalDateTime.now())
                .build());
        //새로 crawling 하기 위해 DB 초기화
        log.debug("Reset DB");
        productRepository.deleteAll();
        //crawling시도 후 성공 메시지와 함께 크롤링 된 상품 갯수 알려줌
        //전체 리스트 객체 생성
        List<ProductDTO> crawlingList;
        List<ProductEntity> resultToEntity;
        ProductEntity productEntity;
        // futures: 크롤링 결과가 담긴 List<CompletableFuture<List<ConvDTO>>>들의 List
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<List<ProductDTO>> gsEventProdList = CompletableFuture.supplyAsync(() -> gsEventCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("GS EVENT 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        CompletableFuture<List<ProductDTO>> gsFreshPbProdList = CompletableFuture.supplyAsync(() -> gsFreshPbCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("GS FRESH PB 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        CompletableFuture<List<ProductDTO>> gsNonFreshPbProdList = CompletableFuture.supplyAsync(() -> gsNonFreshPbCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("GS NON FRESH PB 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        //Seven Eleven EVENT
        CompletableFuture<List<ProductDTO>> sevenEventProdList = CompletableFuture.supplyAsync(() -> sevenEventCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("Seven Eleven EVENT 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        //Seven Eleven PB
        CompletableFuture<List<ProductDTO>> sevenPbProdList = CompletableFuture.supplyAsync(() -> sevenPbCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("Seven Eleven PB 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        //CU EVENT
        CompletableFuture<List<ProductDTO>> cuEventProdList = CompletableFuture.supplyAsync(() -> cuEventCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("CU EVENT 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        //CU PB
        CompletableFuture<List<ProductDTO>> cuPbProdList = CompletableFuture.supplyAsync(() -> cuPbCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("CU PB 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        //EMART EVENT
        CompletableFuture<List<ProductDTO>> emartEventProdList = CompletableFuture.supplyAsync(() -> emartEventCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("EMART EVENT 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        //EMART PB
        CompletableFuture<List<ProductDTO>> emartPbProdList = CompletableFuture.supplyAsync(() -> emartPbCrawling.getProductList(), executor)
                .exceptionally(ex -> {
                    log.error("EMART PB 크롤링 중 에러 발생(ProdService 처리) : {}", ex);
                    log.error("발생위치 : {}", ex.getStackTrace());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
                });
        // allFutures: futures들의 작업이 끝난 것을 합친 CompletableFuture
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        gsEventProdList,
                        gsFreshPbProdList,
                        gsNonFreshPbProdList,
                        sevenEventProdList,
                        sevenPbProdList,
                        cuEventProdList,
                        cuPbProdList,
                        emartEventProdList,
                        emartPbProdList
                )
                .exceptionally(ex -> {
                    // CompletableFuture.allOf 자체에서 발생한 예외 처리
                    log.error("CompletableFuture.allOf 중 예외 발생: {}", ex.getMessage());
                    throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR); // 적절한 예외 처리 필요
                });
        // allConvDTOListFutures: 모든 futures의 결과를 하나의 리스트로 수집하는 CompletableFuture
        CompletableFuture<List<ProductDTO>> allProductResultDTOListFutures = allFutures.thenApply(v -> {
            List<ProductDTO> combinedList = new ArrayList<>();
            try {
                combinedList.addAll(gsEventProdList.join()); // join()을 사용하여 예외를 unchecked 형태로 받음
                combinedList.addAll(gsFreshPbProdList.join());
                combinedList.addAll(gsNonFreshPbProdList.join());
                combinedList.addAll(sevenEventProdList.join());
                combinedList.addAll(sevenPbProdList.join());
                combinedList.addAll(cuEventProdList.join());
                combinedList.addAll(cuPbProdList.join());
                combinedList.addAll(emartEventProdList.join());
                combinedList.addAll(emartPbProdList.join());
            } catch (CompletionException e) {
                throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
            }
            return combinedList;
        }).exceptionally(ex -> {
            // CompletableFuture 체인의 어느 부분에서든 예외가 발생하면 여기서 처리
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        });
        log.debug("ALL CRAWLING FINISH");
        log.debug("CREATE DATA ON DATABASE");
        //AI로 카테고리 분류
        //dto -> entity
        // 결과를 처리하고 저장하는 CompletableFuture
        CompletableFuture<CrawlingResultDTO> resultFuture = allProductResultDTOListFutures.thenApply(allConvDTOList -> {
            log.info("편의점의 총 갯수: {}", allConvDTOList.size());
            List<ProductEntity> productInputData = allConvDTOList.stream()
                    .map(this::productDTOToProductEntity)
                    .collect(Collectors.toList());
            try {
                productRepository.saveAll(productInputData);
                log.debug("SERVICE FINISH");
                return CrawlingResultDTO.builder().resultCount(allConvDTOList.size()).build();
            } catch (Exception ex) {
                log.error("에러 발생 : {}", ex.getMessage());
                crawlingStatus.stopCrawling("productCrawling");
                throw new CustomException(ErrorList.JPA_UNEXPECTED_ERROR);
            }
        });
        return resultFuture.join();
    }

    public boolean checkClientNeedToUpdateProductData(LocalDateTime clientTime) {
        LocalDateTime latestCrawlingTime = crawlingTimeRepository.findById(1L).get().getLatestCrawlingTime();
        log.info("서버 시간 : " + latestCrawlingTime);
        return clientTime.isAfter(latestCrawlingTime);//LocalDateTime이 인자보다 이후인가
    }

    public void updateTopSearched(String productName) {
        SearchEntity searchKeyword = searchRepository.findByProductName(productName);
        if (searchKeyword != null) {
            searchKeyword.incrementSearchCount();
            searchRepository.save(searchKeyword);
            scheduleDeletionOrDecrement(searchKeyword);
        } else {
            searchKeyword = SearchEntity.builder()
                    .productName(productName)
                    .id(UUID.randomUUID().toString())
                    .searchCount(1)
                    .build();
            searchRepository.save(searchKeyword);
            scheduleDeletionOrDecrement(searchKeyword);
        }
    }

    public List<SearchDTO> andFind() {
        //정렬해서 받기
        List<SearchEntity> listSearch = searchRepository.findAll(Sort.by(Sort.Direction.DESC, "searchCount"));
        List<SearchDTO> result = new ArrayList<>();
        SearchDTO one;
        if (listSearch.size() < 10) {
            for (int i = 0; i < listSearch.size(); i++) {
                one = SearchDTO.builder()
                        .productName(listSearch.get(i).getProductName())
                        .build();
                result.add(one);
            }
        } else {
            for (int i = 0; i < 10; i++) {
                one = SearchDTO.builder()
                        .productName(listSearch.get(i).getProductName())
                        .build();
                result.add(one);
            }
        }
        return result;
    }

    private ProductDTO productEntityToProductDTO(ProductEntity productEntity) {
        return ProductDTO.builder()
                .name(productEntity.getPid().getName())
                .convname(productEntity.getPid().getConvname())
                .price(productEntity.getPrice())
                .pb(productEntity.getPb())
                .event(productEntity.getEvent())
                .category(productEntity.getCategory())
                .image(productEntity.getImage())
                .build();
    }

    private ProductEntity productDTOToProductEntity(ProductDTO productDTO) {
        return ProductEntity.builder()
                .pid(new ProductId(productDTO.getName(), productDTO.getConvname()))
                .price(productDTO.getPrice())
                .pb(productDTO.getPb())
                .event(productDTO.getEvent())
                .category(productDTO.getCategory())
                .image(productDTO.getImage())
                .build();
    }

    private void scheduleDeletionOrDecrement(SearchEntity keyword) {
        Runnable task = () -> {
            if (keyword != null) {
                if (keyword.getSearchCount() > 1) {
                    keyword.decrementSearchCount();
                    searchRepository.save(keyword);
                } else {
                    searchRepository.delete(keyword);
                }
            }
        };
        // 24시간 후에 작업을 스케줄합니다.
        taskScheduler.schedule(task, new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
    }
}
