package org.spring.oneplusone.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.spring.Utils.Response.ProductCrawlingAllResponse;
import org.spring.Utils.Response.ProductReadAllResponse;
import org.spring.DTO.ProductDTO;
import org.spring.DTO.CrawlingResultDTO;

import org.spring.oneplusone.Service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {
    //Inject product service class
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "모든 상품 조회", description = "전체 데이터베이스에서 모든 상품을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 모든 상품을 조회하였습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductReadAllResponse.class)))
    })
    @GetMapping("/readAll")
    public ResponseEntity<?> getAllProduct() throws Exception{
        System.out.println("Product ReadAll API START");
        List<ProductDTO> allProductResult = productService.findAllProducts();
        ProductReadAllResponse response = new ProductReadAllResponse(allProductResult, true);
        System.out.println("Product ReadAll API FINISH");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "크롤링 시도", description = "GS25, 세븐일레븐, CU, 이마트에서 상품을 크롤링해서 DB에 저장한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 상품을 crawling 해왔습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductCrawlingAllResponse.class)))
    })
    @PostMapping("/crawling")
    public ResponseEntity<?> crawlingAllProduct() throws Exception{
        System.out.println("Product Crawling API START");
        //crawling시도 후 성공하면 성공 메시지 에러 발생하면 에러 메시지
        CrawlingResultDTO result = productService.productCrawling();
        ProductCrawlingAllResponse response = new ProductCrawlingAllResponse(result, true);
        System.out.println("Product Crawling API FINISH");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
