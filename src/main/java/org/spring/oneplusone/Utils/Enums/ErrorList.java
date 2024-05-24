package org.spring.oneplusone.Utils.Enums;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.security.PrivateKey;

@Getter
@AllArgsConstructor//모든 변수 필요
public enum ErrorList {
    BADREQUEST("BADREQUEST", "request가 잘못 되었습니다", false, HttpStatus.BAD_REQUEST),
    AlreadyCrawling("CRAWLING--001", "CRAWLING-ALREADY-RUNNING",false, HttpStatus.CONFLICT),
    DONTNEEDTOUPDATE("PRODUCT-001", "Client의 Data와 Server의 Data가 동일함",false, HttpStatus.PRECONDITION_FAILED),
    GSEventSelenium("GSEVENT--001", "[GS][행사상품페이지] Selenium에서 웹 요소를 못찾음", false,HttpStatus.CONFLICT),
    GSEventWebElement("GSEVENT--002", "[GS][행사상품페이지] WebElement가 존재하지 않음", false,HttpStatus.CONFLICT),
    GSEventCrawling("GSEVENT--003", "[GS][행사상품페이지] Crawling중 예기치 못한 에러 발생", false,HttpStatus.CONFLICT),
    GSFresh("GSFRESH--001", "GS-FRESH-CRAWLING-ERROR",false, HttpStatus.CONFLICT);

    private final String code;
    private final String errmsg;//여기서만 사용, 변환 X
    private final boolean success;
    private final HttpStatus httpStatus;

}
