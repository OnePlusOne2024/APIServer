package org.spring.oneplusone.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.DTO.CrawlingResultDTO;
import org.spring.oneplusone.Service.ConvService;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Response.ConvenienceReadAllResponse;
import org.spring.oneplusone.Utils.Response.CrawlingAllResponse;
import org.spring.oneplusone.Utils.Response.ErrorResponse;
import org.spring.oneplusone.Utils.Response.ProductReadAllResponse;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1/convenience")
public class ConvController {
    private final ConvService convService;
    private CrawlingStatus crawlingStatus;

    public ConvController(ConvService convService, CrawlingStatus crawlingStatus) {
        this.convService = convService;
        this.crawlingStatus = crawlingStatus;
    }

    @Operation(summary = "편의점 크롤링 시도", description = "GS25, 세븐일레븐, CU, 이마트에서 상품을 크롤링해서 DB에 저장한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 편의점을 크롤링 해왔습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConvenienceReadAllResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Success Example",
                                            summary = "성공 예시",
                                            description = "Success : boolean 타입, resultCount : int 타입",
                                            value = "{\"success\":true, \"result\":{\"resultCount\":3000}}")
                            })),
            @ApiResponse(responseCode = "409", description = "현재 크롤링이 진행중입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error Example",
                                            summary = "에러 예시",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\": false ,\"result\":\"CRAWLING--001\"}")
                            })),
            @ApiResponse(responseCode = "500", description = "크롤링 중에 에러가 발생하였습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error Example 002",
                                            summary = "Crawling Error 002",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\":false,\"result\":\"CRAWLING--002\"}"),
                                    @ExampleObject(
                                            name = "Error Example 003",
                                            summary = "Crawling Error 003",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\":false,\"result\":\"CRAWLING--003\"}"),
                                    @ExampleObject(
                                            name = "Error Example 004",
                                            summary = "Crawling Error 004",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\":false,\"result\":\"CRAWLING--004\"}")
                            }))
    })
    @PostMapping("/crawling")
    public ResponseEntity<?> crawlingConvList() throws CustomException {
        if (crawlingStatus.isCrawling("convenienceCrawling")) {
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
        } finally {
            crawlingStatus.stopCrawling("convenienceCrawling");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "모든 편의점 조회", description = "모든 편의점 데이터를 전부 읽어온다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 모든 편의점을 조회하였습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConvenienceReadAllResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Success Example",
                                            summary = "성공 예시",
                                            description = "convName : String, convBrandName : String, x : double, y : double, convAddr : String",
                                            value = "{\"success\":true,\"result\":{\"convName\":\"편의점 이름\",\"convBrandName\":\"편의점 브랜드 이름\", \"x\":127.0495556,\"y\":37.514575,\"convAddr\":\"편의점 주소\"}}")
                            })),
            @ApiResponse(responseCode = "409", description = "현재 크롤링이 진행중입니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error Example",
                                            summary = "에러 예시",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\": false ,\"result\":\"CRAWLING--001\"}")
                            })),
            @ApiResponse(responseCode = "500", description = "크롤링 중에 에러가 발생하였습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error Example",
                                            summary = "에러 예시",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\":false,\"result\":\"JPA--001\"}")
                            }))
    })
    @GetMapping("/all_conv")
    public ResponseEntity<?> getAllConvList() throws CustomException {
        if (crawlingStatus.isCrawling("convenienceCrawling")) {
            throw new CustomException(ErrorList.ALREADY_CRAWLING);
        }
        log.info("Convenience Read All API START");
        try{
            List<ConvDTO> convList = convService.readConvList();
            ConvenienceReadAllResponse result = ConvenienceReadAllResponse.builder()
                    .result(convList)
                    .success(true)
                    .build();
            log.debug("convenience Read All Api Finish");
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch(CustomException ex){
            throw ex;
        }
    }

    @Operation(summary = "근처 1km 내의 편의점 조회", description = "근처 1km 내의 편의점 데이터를 전부 읽어온다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 근처 1km 내의 편의점을 조회하였습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConvenienceReadAllResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Success Example",
                                            summary = "성공 예시",
                                            description = "convName : String, convBrandName : String, x : double, y : double, convAddr : String",
                                            value = "{\"success\":true,\"result\":{\"convName\":\"편의점 이름\",\"convBrandName\":\"편의점 브랜드 이름\", \"x\":127.0495556,\"y\":37.514575,\"convAddr\":\"편의점 주소\"}}")
                            })),
            @ApiResponse(responseCode = "409", description = "현재 크롤링이 진행중입니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error Example",
                                            summary = "에러 예시",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\": false ,\"result\":\"CRAWLING--001\"}")
                            })),
            @ApiResponse(responseCode = "500", description = "크롤링 중에 에러가 발생하였습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error Example",
                                            summary = "에러 예시",
                                            description = "Success : boolean 타입, result : String 타입",
                                            value = "{\"success\":false,\"result\":\"CRAWLING--002\"}")
                            }))
    })
    @GetMapping("/near_conv")
    public ResponseEntity<?> getNearConvList(
            @Parameter(description = "경도, longitude", example = "127.02761")
            @RequestParam double longitude,
            @Parameter(description = "위도, latitude", example = "37.5665")
            @RequestParam double latitude
    ) throws CustomException {
        if (crawlingStatus.isCrawling("convenienceCrawling")) {
            throw new CustomException(ErrorList.ALREADY_CRAWLING);
        }
        log.info("Convenience Read Near Api START");
        List<ConvDTO> convList = convService.readNearConvList(latitude, longitude);
        ConvenienceReadAllResponse result = ConvenienceReadAllResponse.builder()
                .result(convList)
                .success(true)
                .build();
        log.debug("convenience Read Near Api Finish");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
