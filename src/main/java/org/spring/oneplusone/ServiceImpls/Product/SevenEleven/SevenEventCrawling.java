package org.spring.oneplusone.ServiceImpls.Product.SevenEleven;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.ServiceImpls.Crawling;
import org.spring.oneplusone.Utils.Enums.Category;
import org.spring.oneplusone.Utils.Enums.ConvName;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Enums.URL;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class SevenEventCrawling implements Crawling {
    private CrawlingStatus crawlingStatus;
    public SevenEventCrawling(CrawlingStatus crawlingStatus) {
        this.crawlingStatus = crawlingStatus;
    }

    public List<ProductDTO> getProductList() {
        log.info("SEVEN ELEVEN EVENT CRAWLING START");
        WebDriver driver = startingSession(URL.SEVEN_ELEVEN_EVENT_URL.getUrl());//WebDriver 생성}
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)//FluentWait는 유연한 시간 설정이 가능
                    .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                    .pollingEvery(Duration.ofMillis(1))//5초마다 재확인
                    .ignoring(org.openqa.selenium.NoSuchElementException.class);//Selenium 자체 에러 중 하나
            long intTotalCount = findIntTotalCount(wait);
            log.info("[SEVEN ELEVEN][EVENT] 최대 수량 : {}", intTotalCount);
            //결과 변수 선언
            List<ProductDTO> result = new ArrayList<>();
            //ProductDTO 용 변수 선언
            ProductDTO productInfo;
            String productName;
            ConvName convName = ConvName.SEVENELEVEN;
            int productPrice;
            boolean productPB;
            //상품에 들어갈 행사 종류를 선언하고 초기값은 1+1, 이후 for문마다 다음 메뉴로 이동하고 값을 변경해줌
            String productEvent= "1+1";
            //7eleven은 카테고리 없음 => not found로
            String category = Category.NOT_FOUND.getCategoryName();
            String productImg;
            //변경 값 저장해두기
            String productEvent2 = "2+1";
            String productEvent3 = "할인";
            //다음 카테고리 Webelement 변수
            WebElement nextEvent;
            for(int z = 0; z < 3; z++){//menu 3번 돌기
                //최대 값에서 13을 빼고, 10을 나눈 값 만큼 반복, 0부터 시작이므로 올림 할 필요 없음
                long roopTime = ((intTotalCount - 13) / 10);
                WebElement plusButton;
                for (int i = 0; i < roopTime; i++) {
                    //+버튼 찾기 및 누르기
                    log.debug("[SEVEN][EVENT] {} 번째 +버튼 누름", i);
                    plusButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("btn_more")));
                    plusButton.findElement(By.tagName("a")).click();
                    //jquery가 전부 완료되기까지 기다리기
                    waitForJsExecutor(driver, wait, 0, 5);
                }//ul
                //id List 찾기
                WebElement prodListBlock = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("listDiv")));
                //list에서 li 태그 추출하기
                List<WebElement> productInfoList = prodListBlock.findElements(By.tagName("li"));
                //맨 처음은 행사 정보 표시이니까 삭제
                productInfoList.remove(0);
                //li 태그 만큼 for문
                for(WebElement product : productInfoList){
                    productName = product.findElement(By.xpath("./div[@class='pic_product']/div[@class='infowrap']/div[@class='name']")).getText();
                    productPrice = Integer.parseInt(product.findElement(By.xpath("./div[@class='pic_product']/div[@class='infowrap']/div[@class='price']")).getText().replace(",",""));
                    if(product.findElement(By.className("tag_list_01")).getSize().equals(1)){
                        productPB = false;
                    }else{
                        productPB = true;
                    }
                    productImg = product.findElement(By.xpath("./div[@class='pic_product']/img")).getAttribute("src");
                    productInfo = ProductDTO.builder()
                            .name(productName)
                            .convname(convName)
                            .price(productPrice)
                            .pb(productPB)
                            .event(productEvent)
                            .category(category)
                            .image(productImg)
                            .build();
                    //정보 등록
                    result.add(productInfo);
                    log.debug("[SEVENELEVEN][EVENT]상품명 : {}, 가격 : {}, 행사 종류 : {}, ", productName, productPrice, productEvent);
                }
                //다음 메뉴 버튼 찾기 및 이동 및 값 변경
                if(z == 0){
                    nextEvent = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wrap_tab")));
                    nextEvent.findElements(By.tagName("a")).get(1).click();
                    productEvent = productEvent2;
                } else if (z == 1) {
                    nextEvent = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wrap_tab")));
                    nextEvent.findElements(By.tagName("a")).get(3).click();
                    productEvent = productEvent3;
                }
                String currentUrl = driver.getCurrentUrl();
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(currentUrl)));
                waitForJsExecutor(driver, wait, 0, 5);
            }
            log.info("SEVEN ELEVEN EVENT CRAWLING FINISH");
            driver.quit();
            return result;

        }catch (org.openqa.selenium.NoSuchElementException e) {
            // Selenium의 NoSuchElementException을 커스텀 예외로 변환하여 throw
            log.error("[SEVENELEVEN][EVENT]Selenium Error : NoSuchElementException");
            log.error("detail : {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (WebDriverException e) {
            log.error("[SEVENELEVEN][EVENT]Selenium Error : WebDriverException");
            log.error("detail : {}", e);
            // Selenium의 일반적인 WebDriverException을 커스텀 예외로 변환하여 throw
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("[SEVENELEVEN][EVENT] {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }

    }
    private void waitForJsExecutor(WebDriver driver, Wait<WebDriver> wait, int attempts, int maxAttempts) {
        while (attempts < maxAttempts) {//네트워크 에러가 떳을 경우에 재시도 하기
            log.debug("[Seven Eleven]Execute jsExecutor");
            log.debug("[Seven Eleven]ajax가 완료되기 까지 기다리기");
            log.debug("[Seven Eleven]시도 횟수 : {}", attempts+1);
            try {
                wait.until((WebDriver d) -> {
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) d;
                    return (Boolean) jsExecutor.executeScript("return jQuery.active == 0;");
                });
                break;
            } catch (UnhandledAlertException e) {
                try{
                    log.error("Unexpected alert occurred: {}", e.getAlertText());
                    // 여기에서 알림을 처리할 수 있습니다. 예를 들어, 알림을 닫습니다.
                    driver.switchTo().alert().accept();
                    // attempts를 증가시킨 후 현재 순서를 재시도합니다.
                    attempts++;
                }catch (NoAlertPresentException ex) {
                    log.error("No alert present: {}", ex.getMessage());
                    // 알림이 없는 경우의 처리 로직
                    attempts++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private long findIntTotalCount(Wait<WebDriver> wait){
        log.debug("SEVENELEVEN][EVENT] 최대 페이지 찾기");
        return (long) wait.until((WebDriver d) -> {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) d;
            String script = "function getNum(){\n" +
                    "    var fncMoreSource = fncMore.toString();\n" +
                    "    var intTotalCountRegex = /intTotalCount\\s*=\\s*\"(\\d+)\"/;\n" +
                    "    var match = fncMoreSource.match(intTotalCountRegex);\n" +
                    "    if (match) {\n" +
                    "      var intTotalCount = parseInt(match[1]);\n" +
                    "      return intTotalCount;\n" +
                    "    }\n" +
                    "    return 0;\n" +
                    "}\n" +
                    "return getNum();";
            return (long) jsExecutor.executeScript(script);
        });
    }

}

