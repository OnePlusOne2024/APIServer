package org.spring.oneplusone.ServiceImpls;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.ArrayList;
import java.util.List;

//Crawling interface 정의
public interface Crawling {
    // WebDriver 인스턴스를 추적하기 위한 리스트
    List<WebDriver> activeWebDrivers = new ArrayList<>();
    //SevenEleven
    //Emart
    //CU1

    //Crawlling을 위한 세션을 생성하고 해당 객체를 가져오는 기본 method
    default WebDriver startingSession(String webStiePath){//나중에 enum으로 바꾸기
        //Chrome Option 사용 위한 것
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");//헤드리스로 열기
        //ChromeDriverService를 사용하기 위한 포석
        //ChromeDriverService에는 객체를 생성할 때 bulder패턴을 통해 설정을 변경 가능
        ChromeDriverService service = new ChromeDriverService.Builder()
                .withLogOutput(System.out)//console창에 로그 출력
//                .usingDriverExecutable(driverpath) 추후에 driver경로를 설정 하고 싶을 때 사용
                .build();
        //해당 객체를 변수로 가지는 ChromerDriver객체 생성
        WebDriver driver = new ChromeDriver(service, options);
        driver.get(webStiePath);

        // WebDriver 인스턴스를 추적 리스트에 추가
        synchronized (activeWebDrivers) {
            activeWebDrivers.add(driver);
        }
        return driver;
    }
    //정확한 부분을 크롤링 해오는 추상 method
    //interface는 자동 public
//    List<ProductDTO> getProduct();
    //해당 메서드가 여러차례 다르게 재정의 되야 될 거 같아서 명시X, 나중에 추상클래스 등을 고려해보기

    //모든 WebDriver를 삭제해주는 매서드
    default void stopAllCrawling() {
        synchronized (activeWebDrivers) {
            for (WebDriver driver : activeWebDrivers) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            activeWebDrivers.clear(); // 리스트 비우기
        }
    }
}


