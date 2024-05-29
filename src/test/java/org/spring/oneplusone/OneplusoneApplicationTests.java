package org.spring.oneplusone;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Enums.URL;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@SpringBootTest
class OneplusoneApplicationTests {

    @Test
    void contextLoads() {
//        WebDriver driver = startingSession(URL.SEVEN_ELEVEN_CONV_LIST.getUrl());
//        try {
//			Wait<WebDriver> wait = new FluentWait<>(driver)
//					.withTimeout(Duration.ofSeconds(40))
//					.pollingEvery(Duration.ofMillis(1))
//					.ignoring(NoSuchElementException.class);
//			WebElement findStoreButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("util_store store_open")));
//			findStoreButton.click();
//			wait.until(ExpectedConditions.presenceOfElementLocated(By.className("overlayerBg")));
//			WebElement storeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("storeForm")));
//			WebElement siDoList = storeElement.findElement(By.name("storeLaySido"));
//			WebElement guList = storeElement.findElement(By.name("storeLayGu"));
//			WebElement storeCheckButton = storeElement.findElement(By.id("storeButton1"));
//            Select siDoSelect = new Select(siDoList);
//            Select guSelect;
//            List<WebElement> siDoOptions = siDoSelect.getOptions();
//            List<WebElement> guOptions;
//            List<WebElement> storeLists;
//            WebElement storeListWebElement;
//            List<WebElement> spanListInListStore;
//            List<ConvDTO> result;
//            String siDoValue;
//            String guValue;
//            String convName;
//            String convAddr;
//            WebElement storeAddress;
//            double x;
//            double y;
//            Pattern pattern = Pattern.compile("markerClick\\(\\d+,(\\d+\\.\\d+),(\\d+\\.\\d+)\\)");
//            Matcher matcher;
//            //시,도 for문
//            for(WebElement siDoOption : siDoOptions){
//                //구 안에서 for문
//                siDoValue = siDoOption.getAttribute("value");
//                siDoSelect.selectByValue(siDoValue);
//                System.out.println("debug) 시/도 : "+siDoValue);
//                guSelect = new Select(guList);
//                guOptions = guSelect.getOptions();
//                for(WebElement guOption : guOptions){
//                    guValue = guOption.getAttribute("value");
//                    guSelect.selectByValue(guValue);
//                    System.out.println("debug) 구 : "+guValue);
//                    storeCheckButton.click();
//                    storeListWebElement = storeElement.findElement(By.className("list_stroe"));
//                    storeLists = storeListWebElement.findElements(By.tagName("li"));
//                    for(WebElement storeList : storeLists){
//                        spanListInListStore = storeList.findElements(By.tagName("span"));
//                        convName = spanListInListStore.get(0).getText();
//                        convAddr = spanListInListStore.get(1).getText();
//                        storeAddress = storeList.findElement(By.tagName("a"));
//                        String addressHref = storeAddress.getAttribute("href");
//                        matcher = pattern.matcher(addressHref);
//                        if(matcher.find()){
//                            x = Double.parseDouble(matcher.group(1));
//                            y = Double.parseDouble(matcher.group(2));
//                            System.out.println("ConvName : "+ convName+", ConvAddr : "+convAddr+", x : "+x+", y : " +y);
//                        }
//                        //convBrandName
//                    }
//                }
//            }
//        } catch (Exception e) {
//            driver.quit();
////            crawlingStatus.stopCrawling("convenienceCrawling");
//            System.out.println("에러 디테일 : " + e);
////            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
//        } finally {
//            driver.quit();
//        }
    }

    WebDriver startingSession(String webStiePath) {//나중에 enum으로 바꾸기
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
        WebDriver driver = new ChromeDriver(service);
        driver.get(webStiePath);
        //가져오는 거 성공했다는 로그 남기기
//        log.info(driver.getTitle());
        return driver;
    }
}
