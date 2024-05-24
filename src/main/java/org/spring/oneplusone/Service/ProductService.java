package org.spring.oneplusone.Service;


import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.Entity.CrawlingTimeEntity;
import org.spring.oneplusone.Entity.ProductEntity;
import org.spring.oneplusone.Entity.ProductId;
import org.spring.oneplusone.Repository.CrawlingTimeRepository;
import org.spring.oneplusone.Repository.ProductRepository;
import org.spring.oneplusone.ServiceImpls.GsCrawling;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
    //Dependency Injection을 위해 생성자를 주입
    private final ProductRepository productRepository;
    private final CrawlingTimeRepository crawlingTimeRepository;
    private final GsCrawling gsCrawling;
    public ProductService(ProductRepository productRepository, CrawlingTimeRepository crawlingTimeRepository, GsCrawling gsCrawling){
        this.productRepository = productRepository;
        this.crawlingTimeRepository = crawlingTimeRepository;
        this.gsCrawling = gsCrawling;
    }

    public List<ProductDTO> findAllProducts(){
        log.debug("SERVICE START");
        //repository를 통해서 DB 접속
        //read all
            List<ProductEntity> productList = productRepository.findAll();
            log.info("현재 물품 수량 : " + productList.size());
            List<ProductDTO> resultWithinDTO = productList.stream().map(this::productEntityToProductDTO).collect(Collectors.toList());
            log.debug("SERVICE FINISH");
            return resultWithinDTO;

    }
    //ProductEntity를 ProductDTO로변환해주는 메서드


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
        List<ProductEntity> resultToEntity ;
        ProductEntity productEntity;
        //GS크롤링 Object 생성
        crawlingList = gsCrawling.getEventProduct();
        //SEVENELEVEN크롤링
        //멀티 쓰레드 추가?
        log.debug("ALL CRAWLING FINISH");
        log.debug("CREATE DATA ON DATABASE");
        //AI로 카테고리 분류
        //dto -> entity
        resultToEntity =crawlingList.stream().map(this::productDTOToProductEntity).collect(Collectors.toList());
        //crawling후에 DB에 등록
        productRepository.saveAll(resultToEntity);
        //결과 return
        //나중에 spring bean에서 가져오는 걸로 수정
        CrawlingResultDTO crawlingResult = new CrawlingResultDTO(resultToEntity.size());
        log.debug("SERVICE FINISH");
        return crawlingResult;
    }

    public boolean checkClientNeedToUpdateProductData(LocalDateTime clientTime){
        LocalDateTime latestCrawlingTime = crawlingTimeRepository.findById(1L).get().getLatestCrawlingTime();
        log.info("서버 시간 : " + latestCrawlingTime);
        return clientTime.isAfter(latestCrawlingTime);//LocalDateTime이 인자보다 이후인가
    }
    private ProductDTO productEntityToProductDTO(ProductEntity productEntity){
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
    private ProductEntity productDTOToProductEntity(ProductDTO productDTO){
        return ProductEntity.builder()
                .pid(new ProductId(productDTO.getName(), productDTO.getConvname()))
                .price(productDTO.getPrice())
                .pb(productDTO.getPb())
                .event(productDTO.getEvent())
                .category(productDTO.getCategory())
                .image(productDTO.getImage())
                .build();
    }
}
