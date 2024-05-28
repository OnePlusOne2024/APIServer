package org.spring.oneplusone.Utils.Enums;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.security.PrivateKey;

@Getter
@AllArgsConstructor//모든 변수 필요
public enum ErrorList {
    BAD_REQUEST("BADREQUEST", "request가 잘못 되었습니다", false, HttpStatus.BAD_REQUEST),
    ALREADY_CRAWLING("CRAWLING--001", "CRAWLING-ALREADY-RUNNING",false, HttpStatus.CONFLICT),
    DO_NOT_NEED_UPDATE("PRODUCT-001", "Client의 Data와 Server의 Data가 동일함",false, HttpStatus.PRECONDITION_FAILED),
    CRAWLING_SELENIUM("CRAWLING--002", "Selenium에서 웹 요소를 못찾음", false,HttpStatus.INTERNAL_SERVER_ERROR),
    CRAWLING_WEB_ELEMENT("CRAWLING--003", "WebElement가 존재하지 않음", false,HttpStatus.INTERNAL_SERVER_ERROR),
    CRAWLING_UNEXPECTED_ERROR("CRAWLING--004", "Crawling중 예기치 못한 에러 발생", false,HttpStatus.INTERNAL_SERVER_ERROR),
    JPA_UNEXPECTED_ERROR("JPA--001", "JPA로 DB에 저장하는 중 예기치 못한 에러가 발생했습니다.", false, HttpStatus.INTERNAL_SERVER_ERROR),
    GS_FRESH("GSFRESH--001", "GS-FRESH-CRAWLING-ERROR",false, HttpStatus.CONFLICT),
    GS_CONV("CONV--001","GS 편의점 CRAWLING 중에 에러가 발생했습니다", false, HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String errmsg;//여기서만 사용, 변환 X
    private final boolean success;
    private final HttpStatus httpStatus;

}
