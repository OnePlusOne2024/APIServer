package org.spring.oneplusone.Utils.Enums;

import lombok.Getter;

@Getter
public enum URL {
    //각 사이트 경로를 변수로 선언, 나중에 사이트 경로 수정해야될 때 해당 변수만 수정하면 됨
    //GS25
    GS_EVENT_URL("http://gs25.gsretail.com/gscvs/ko/products/event-goods"),
    GS_PB_FRESH_URL("http://gs25.gsretail.com/gscvs/ko/products/youus-freshfood"),
    GS_PB_NOT_FRESHURL("http://gs25.gsretail.com/gscvs/ko/products/youus-different-service"),
    GS_CONV_LIST("http://gs25.gsretail.com/gscvs/ko/store-services/locations#;"),
    SEVEN_ELEVEN_CONV_LIST("http://www.7-eleven.co.kr/");

    private final String url;
    //생성자를 통해서 URL 삽입
    URL(String url){
        this.url = url;
    }
}
