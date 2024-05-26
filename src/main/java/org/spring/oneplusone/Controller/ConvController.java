package org.spring.oneplusone.Controller;

import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.Service.ConvService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/convenience")
public class ConvController {
    private ConvService convService;
    public ConvController(ConvService convService){
        this.convService = convService;
    }
    @PostMapping("/crawling")
    public ResponseEntity<?> crawlingConvList(){
        log.info("Crawling API START");

    }
}
