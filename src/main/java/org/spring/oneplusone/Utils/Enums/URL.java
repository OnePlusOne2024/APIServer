package org.spring.oneplusone.Utils.Enums;

import lombok.Getter;

@Getter
public enum URL {
    //각 사이트 경로를 변수로 선언, 나중에 사이트 경로 수정해야될 때 해당 변수만 수정하면 됨
    //GS25
    GS_EVENT_URL("http://gs25.gsretail.com/gscvs/ko/products/event-goods"),
    GS_PB_FRESH_URL("http://gs25.gsretail.com/gscvs/ko/products/youus-freshfood"),
    GS_PB_NOT_FRESH_URL("http://gs25.gsretail.com/gscvs/ko/products/youus-different-service"),
    //Emart
    EMART_EVENT_URL("https://emart24.co.kr/goods/event"),
    EMART_PB_URL("https://emart24.co.kr/goods/pl"),
    //CU
    CU_PB_URL("https://cu.bgfretail.com/product/pb.do?category=product&depth2=1&sf=N"),
    CU_EVENT_URL("https://cu.bgfretail.com/event/plus.do?category=event&depth2=1&sf=N"),
    //SevenEleven
    SEVEN_ELEVEN_EVENT_URL("https://www.7-eleven.co.kr/product/presentList.asp"),
    SEVEN_ELEVEN_PB_URL("https://www.7-eleven.co.kr/product/7prodList.asp"),
    //편의점 리스트
    GS_CONV_URL("http://gs25.gsretail.com/gscvs/ko/store-services/locations#;"),
    CU_CONV_URL("https://cu.bgfretail.com/store/list.do?category=store"),
    SEVEN_ELEVEN_CONV_LIST("http://www.7-eleven.co.kr/"),
    EMART_CONV_URL("https://emart24.co.kr/store");

    private final String url;
    //생성자를 통해서 URL 삽입
    URL(String url){
        this.url = url;
    }
}
