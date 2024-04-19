package org.spring.oneplusone.ServiceImpls;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.Global.Enums.URL;

import java.util.List;

public class GsCrawling implements Crawling{


    public List<ProductDTO> getEventProduct() {
        //enum에 선언된 url을 통해 크롤링 시도

        WebDriver driver = startingSession(URL.GSEVENTURL.getUrl());
        return null;
    }
}
