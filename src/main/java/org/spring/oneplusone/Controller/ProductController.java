package org.spring.oneplusone.Controller;

import io.swagger.v3.oas.annotations.Operation;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.DTO.CrawlingResultDTO;

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

    @Operation(summary = "DB읽어오기", description = "전체 DB를 읽어온다")
    @GetMapping("/readAll")
    public ResponseEntity<?> getAllProduct() throws Exception{
        List<ProductDTO> allProductResult = productService.findAllProducts();
        return new ResponseEntity<>(allProductResult, HttpStatus.OK);
    }

    @PostMapping("/crawling")
    public ResponseEntity<?> crawlingAllProduct() throws Exception{
        //crawling시도 후 성공하면 성공 메시지 에러 발생하면 에러 메시지
        CrawlingResultDTO result = productService.productCrawling();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
