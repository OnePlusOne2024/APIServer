package org.spring.oneplusone.Utils.Enums;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.security.PrivateKey;

@Getter
@AllArgsConstructor//모든 변수 필요
public enum ErrorList {
    GSEVENT("GSEVENT--001", "GS-EVENT-CRAWLING-ERROR", HttpStatus.CONFLICT),
    GSFRESHPB("GSFRESH--001", "GS-FRESH-CRAWLING-ERROR", HttpStatus.CONFLICT);

    private final String errmsg;//여기서만 사용, 변환 X
    private final String code;
    private final HttpStatus httpStatus;

    //생성자를 통해서 ErrorMessgae 삽입
    //따로 생성하지는 않기 때문에 생성자는 삭제(추측)
//    ErrorList(String code, String errmsg, HttpStatus httpStatus){
//        this.code = code;
//        this.httpStatus = httpStatus;
//        this.errmsg = errmsg;
//    }
}
