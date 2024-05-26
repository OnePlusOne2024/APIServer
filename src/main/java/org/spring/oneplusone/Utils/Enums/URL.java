package org.spring.oneplusone.Utils.Enums;

import lombok.Getter;

@Getter
public enum URL {
    //각 사이트 경로를 변수로 선언, 나중에 사이트 경로 수정해야될 때 해당 변수만 수정하면 됨
    //GS25
    GSEVENTURL("http://gs25.gsretail.com/gscvs/ko/products/event-goods"),
    GSPBFRESHURL("http://gs25.gsretail.com/gscvs/ko/products/youus-freshfood"),
    GSPBNOTFRESHURL("http://gs25.gsretail.com/gscvs/ko/products/youus-different-service"),
    GSCONVLIST("http://gs25.gsretail.com/gscvs/ko/store-services/locations#;");

    private final String url;
    //생성자를 통해서 URL 삽입
    URL(String url){
        this.url = url;
    }
}
