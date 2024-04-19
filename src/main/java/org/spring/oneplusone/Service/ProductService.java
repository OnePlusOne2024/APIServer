package org.spring.oneplusone.Service;


import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.Entity.ProductEntity;
import org.spring.oneplusone.Repository.ProductRepository;
import org.spring.oneplusone.ServiceImpls.GsCrawling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    //여기서만 사용되는 final 객체
    private final ProductRepository productRepository;
    //해당 객체를 통해서 만들어진 Repository 생성자
    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<ProductDTO> findAllProducts(){
        //repository를 통해서 DB 접속
        //read all
        List<ProductEntity> productList = productRepository.findAll();
        List<ProductDTO> resultWithinDTO =productList.stream().map(this::productDtoToProductEntity).collect(Collectors.toList());
        return resultWithinDTO;
    }
    //ProductEntity를 ProductDTO로변환해주는 메서드
    private ProductDTO productDtoToProductEntity(ProductEntity productEntity){
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

    public CrawlingResultDTO productCrawling(){
        //crawling시도 후 성공 메시지와 함께 크롤링 된 상품 갯수 알려줌
        //전체 리스트 객체 생성
        List<ProductDTO> result;
        //GS크롤링 Object 생성
        try{
            GsCrawling crawlingGS = new GsCrawling();
            crawlingGS.getEventProduct();
        } catch(error){

        }
        //SEVENELEVEN크롤링

        //crawling후에 DB에 등록
        //결과 return
        CrawlingResultDTO crawlingResult = CrawlingResultDTO.builder()
                .success(Boolean.TRUE)
                .resultCount(result.size())
                .build();

    }
}
