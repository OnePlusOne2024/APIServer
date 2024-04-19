package org.spring.oneplusone.ServiceImpls;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.spring.oneplusone.DTO.ProductDTO;

import java.util.List;

//Crawling interface 정의
public interface Crawling {

    //SevenEleven
    //Emart
    //CU1

    //Crawlling을 위한 세션을 생성하고 해당 객체를 가져오는 기본 method
    default WebDriver startingSession(String webStiePath){
        WebDriver driver = new ChromeDriver();
        driver.get(webStiePath);
        return driver;
    }
    //정확한 부분을 크롤링 해오는 추상 method
    //interface는 자동 public
//    List<ProductDTO> getProduct();
    //해당 메서드가 여러차례 다르게 재정의 되야 될 거 같아서 명시X, 나중에 추상클래스 등을 고려해보기
}


