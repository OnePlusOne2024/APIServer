package org.spring.oneplusone.Controller;

import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.Service.ConvService;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Response.ConvenienceReadAllResponse;
import org.spring.oneplusone.Utils.Response.CrawlingAllResponse;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/convenience")
public class ConvController {
    private final ConvService convService;
    @Autowired
    private CrawlingStatus crawlingStatus;
    public ConvController(ConvService convService){
        this.convService = convService;
    }
    @PostMapping("/crawling")
    public ResponseEntity<?> crawlingConvList() throws CustomException {
        if(crawlingStatus.isCrawling("convenienceCrawling")){
            throw new CustomException(ErrorList.ALREADY_CRAWLING);
        }
        log.info("Convenience Crawling API START");
        crawlingStatus.startCrawling("convenienceCrawling");
        CrawlingAllResponse response;
        try {
            CrawlingResultDTO result = convService.convCrawling();
            response = CrawlingAllResponse.builder()
                    .result(result)
                    .success(true)
                    .build();
            log.info("Convenience Crawling API Finish");
        }finally {
            crawlingStatus.stopCrawling("convenienceCrawling");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/all_conv")
    public ResponseEntity<?> getAllConvList() throws CustomException{
        if(crawlingStatus.isCrawling("convenienceCrawling")){
            throw new CustomException(ErrorList.ALREADY_CRAWLING);
        }
        log.info("Convenience Read All API START");
        List<ConvDTO> convList = convService.readConvList();
        ConvenienceReadAllResponse result = ConvenienceReadAllResponse.builder()
                .result(convList)
                .success(true)
                .build();
        log.debug("convenience Read All Api Finish");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping("/near_conv")
    public ResponseEntity<?> getNearConvList(@RequestParam double x, @RequestParam double y) throws CustomException{
        if(crawlingStatus.isCrawling("convenienceCrawling")){
            throw new CustomException(ErrorList.ALREADY_CRAWLING);
        }
        log.info("Convenience Read Near Api START");
        List<ConvDTO> convList = convService.readNearConvList(x,y);
        ConvenienceReadAllResponse result = ConvenienceReadAllResponse.builder()
                .result(convList)
                .success(true)
                .build();
        log.debug("convenience Read Near Api Finish");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
