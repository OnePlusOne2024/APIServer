package org.spring.oneplusone.Global.Enums;

import lombok.Getter;

import java.security.PrivateKey;

@Getter
public enum ErrorList {
    GSEVENT("GS-EVENT-CRAWLING-ERROR"),
    GSFRESHPB("GS-FRESH-CRAWLING-ERROR");

    private String errmsg;

    //생성자를 통해서 ErrorMessgae 삽입
    ErrorList(String errmsg){
        this.errmsg = errmsg;
    }
}
