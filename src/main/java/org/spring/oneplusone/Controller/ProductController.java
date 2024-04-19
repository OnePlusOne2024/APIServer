package org.spring.oneplusone.Controller;

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

    @GetMapping("/readAll")
    public ResponseEntity<?> getAllProduct() throws Exception{

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/crawling")
    public ResponseEntity<?> crawlingAllProduct() {
        //crawling시도 후 성공하면 성공 메시지 에러 발생하면 에러 메시지
        CrwalingResultDTO result = productService.productCrawling();

    }
}
