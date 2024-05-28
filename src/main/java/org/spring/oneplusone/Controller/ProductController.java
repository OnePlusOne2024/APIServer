package org.spring.oneplusone.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Response.CrawlingAllResponse;
import org.spring.oneplusone.Utils.Response.ErrorResponse;
import org.spring.oneplusone.Utils.Response.ProductReadAllResponse;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.DTO.CrawlingResultDTO;

import org.spring.oneplusone.Service.ProductService;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {
    //Inject product service class
    private final ProductService productService;
    @Autowired
    private CrawlingStatus crawlingStatus;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "모든 상품 조회", description = "전체 데이터베이스에서 모든 상품을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 모든 상품을 조회하였습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductReadAllResponse.class))),
            @ApiResponse(responseCode = "412", description = "Client의 데이터와 Server의 데이터가 동일합니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "현재 크롤링이 진행중입니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/read_all")
    public ResponseEntity<?> getAllProduct(String clientTime) throws CustomException{
        log.info("Product ReadAll API START");
        LocalDateTime dateTime = LocalDateTime.parse(clientTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        log.info("Client시간: " + dateTime);
        //client에 저장된 시간이 server에 저장된 시간 이후이므로 update할 필요가 없음
        if(productService.checkClientNeedToUpdateProductData(dateTime)){

            throw new CustomException(ErrorList.DO_NOT_NEED_UPDATE);
        }
        //Crawling이 진행중이면 해당 요청을 처리할 수 없음
        if(crawlingStatus.isCrawling("productCrawling")){
            throw new CustomException(ErrorList.ALREADY_CRAWLING);
        }
        List<ProductDTO> allProductResult = productService.findAllProducts();
        ProductReadAllResponse response = ProductReadAllResponse.builder()
                .success(true)
                .result(allProductResult)
                .build();
        log.info("Product ReadAll API FINISH");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "크롤링 시도", description = "GS25, 세븐일레븐, CU, 이마트에서 상품을 크롤링해서 DB에 저장한다.")
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "성공적으로 상품을 crawling 해왔습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CrawlingAllResponse.class))),
            @ApiResponse(responseCode = "409", description = "현재 크롤링이 진행중입니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/crawling")
    public ResponseEntity<?> crawlingAllProduct() throws CustomException {
        //먼저 시도되고 있는 crawling이 있는지 확인하기
        if(crawlingStatus.isCrawling("productCrawling")){
            throw new CustomException(ErrorList.ALREADY_CRAWLING);
        }
        log.info("Product Crawling API START");
        crawlingStatus.startCrawling("productCrawling");
        CrawlingAllResponse response;
        try{
            //crawling시도 후 성공하면 성공 메시지 에러 발생하면 에러 메시지
            CrawlingResultDTO result = productService.productCrawling();
            response = CrawlingAllResponse.builder()
                    .result(result)
                    .success(true)
                    .build();
            log.info("Product Crawling API FINISH");
        } finally {
            crawlingStatus.stopCrawling("productCrawling");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
