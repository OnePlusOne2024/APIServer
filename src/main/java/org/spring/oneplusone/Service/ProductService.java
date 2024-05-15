package org.spring.oneplusone.Service;


import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.Entity.ProductEntity;
import org.spring.oneplusone.Entity.ProductId;
import org.spring.oneplusone.Repository.ProductRepository;
import org.spring.oneplusone.ServiceImpls.GsCrawling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    //Dependency Injection을 위해 생성자를 주입
    private final ProductRepository productRepository;
    private final GsCrawling gsCrawling;
    public ProductService(ProductRepository productRepository, GsCrawling gsCrawling){
        this.productRepository = productRepository;
        this.gsCrawling = gsCrawling;
    }



    public List<ProductDTO> findAllProducts(){
        System.out.println("SERVICE START");
        //repository를 통해서 DB 접속
        //read all
        List<ProductEntity> productList = productRepository.findAll();
        System.out.println("읽은 데이터 수 : "+productList.size());
        List<ProductDTO> resultWithinDTO =productList.stream().map(this::productEntityToProductDTO).collect(Collectors.toList());
        System.out.println("SERVICE FINISH");
        return resultWithinDTO;
    }
    //ProductEntity를 ProductDTO로변환해주는 메서드


    public CrawlingResultDTO productCrawling() throws Exception {
        System.out.println("SERVICE START");
        //새로 crawling 하기 위해 DB 초기화
        System.out.println("Reset DB");
        productRepository.deleteAll();
        //crawling시도 후 성공 메시지와 함께 크롤링 된 상품 갯수 알려줌
        //전체 리스트 객체 생성
        List<ProductDTO> crawlingList;
        List<ProductEntity> resultToEntity ;
        ProductEntity productEntity;
        //GS크롤링 Object 생성
        crawlingList = gsCrawling.getEventProduct();
        //SEVENELEVEN크롤링


        System.out.println("ALL CRAWLING FINISH");
        System.out.println("CREATE DATA ON DATABASE");
        //dto -> entity
        resultToEntity =crawlingList.stream().map(this::productDTOToProductEntity).collect(Collectors.toList());
        //crawling후에 DB에 등록
        productRepository.saveAll(resultToEntity);
        //결과 return
        //나중에 spring bean에서 가져오는 걸로 수정
        CrawlingResultDTO crawlingResult = new CrawlingResultDTO(true, resultToEntity.size());
        System.out.println("SERVICE FINISH");
        return crawlingResult;
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
