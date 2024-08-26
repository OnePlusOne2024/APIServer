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
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class SevenPbCrawling implements Crawling {
    private CrawlingStatus crawlingStatus;

    public SevenPbCrawling(CrawlingStatus crawlingStatus) {
        this.crawlingStatus = crawlingStatus;
    }

    public List<ProductDTO> getProductList() {
        log.info("SEVEN ELEVEN PB CRAWLING START");
        WebDriver driver = startingSession(URL.SEVEN_ELEVEN_PB_URL.getUrl());//WebDriver 생성}
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)//FluentWait는 유연한 시간 설정이 가능
                    .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                    .pollingEvery(Duration.ofMillis(1))//5초마다 재확인
                    .ignoring(org.openqa.selenium.NoSuchElementException.class);//Selenium 자체 에러 중 하나
            //최대 값 추출
            long intTotalCount = findIntTotalCount(wait);
            log.info("[SEVENELEVEN][PB]상품 수량 : {}", intTotalCount);
            //결과 변수 선언
            List<ProductDTO> result = new ArrayList<>();
            //ProductDTO 용 변수 선언
            ProductDTO productInfo;
            String productName;
            ConvName convName = ConvName.SEVENELEVEN;
            int productPrice;
            boolean productPB = true;
            String productEvent = "none";
            //7eleven은 카테고리 없음 => not found로
            String category = Category.NOT_FOUND.getCategoryName();
            String productImg;
            //최대 값에서 13을 빼고, 10을 나눈 값 만큼 반복, 0부터 시작이므로 올림 할 필요 없음
            long roopTime = ((intTotalCount - 13) / 10);
            WebElement plusButton;
            for (int i = 0; i < roopTime; i++) {
                //+버튼 찾기 및 누르기
                log.debug("[SEVENELEVEN][PB] {}번째 버튼 누르기", i+1);
                plusButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("btn_more")));
                plusButton.findElement(By.tagName("a")).click();
                //jquery가 전부 완료되기까지 기다리기
                waitForJsExecutor(driver, wait, 0, 5);
            }
            //id List 찾기
            WebElement prodListBlock = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("listUl")));
            //list에서 li 태그 추출하기
            List<WebElement> productInfoList = prodListBlock.findElements(By.tagName("li"));
            //맨 처음은 행사 정보 표시이니까 삭제
            productInfoList.remove(0);
            //li 태그 만큼 for문
            for (WebElement product : productInfoList) {
                log.debug(product.getTagName());
                productName = product.findElement(By.xpath("//*/div[@class='pic_product']/div[@class='infowrap']/div[@class='name']")).getText();
                log.debug(productName);
                productPrice = Integer.parseInt(product.findElement(By.xpath("//*/div[@class='pic_product']/div[@class='infowrap']/div[@class='price']")).getText().replace(",",""));
                log.debug("값 : {}",productPrice);
                if (!product.findElement(By.xpath("//*/ul[@class='tag_list_01']/li")).getSize().equals(1)) {
                    //이미 Event를 한 뒤이기 때문에 Event를 하는 Pb상품을 기다릴 필요가 없음
                    continue;
                }
                productImg = "https://www.7-eleven.co.kr/" + product.findElement(By.xpath("//*/div[@class='pic_product']/img")).getAttribute("src");
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
                log.debug("[SEVENELEVEN][PB]상품명 : {}, 가격 : {}, 행사 종류 : {}, ", productName, productPrice, productEvent);
            }
            log.info("SEVEN ELEVEN PB CRAWLING FINISH");
            driver.quit();
            return result;

        }catch (org.openqa.selenium.NoSuchElementException e) {
            // Selenium의 NoSuchElementException을 커스텀 예외로 변환하여 throw
            log.error("[SEVENELEVEN][PB]Selenium Error : NoSuchElementException");
            log.error("detail : {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (WebDriverException e) {
            log.error("[SEVENELEVEN][PB]Selenium Error : WebDriverException");
            log.error("detail : {}", e);
            // Selenium의 일반적인 WebDriverException을 커스텀 예외로 변환하여 throw
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("[SEVENELEVEN][PB] {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }

    }

    private void waitForJsExecutor(WebDriver driver, Wait<WebDriver> wait, int attempts, int maxAttempts) {
        while (attempts < maxAttempts) {//네트워크 에러가 떳을 경우에 재시도 하기
            log.debug("[Seven Eleven]Execute jsExecutor");
            log.debug("[Seven Eleven]ajax가 완료되기 까지 기다리기");
            log.debug("[Seven Eleven]시도 횟수 : {}", attempts + 1);
            try {
                wait.until((WebDriver d) -> {
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) d;
                    return (Boolean) jsExecutor.executeScript("return jQuery.active == 0;");
                });
                break;
            } catch (UnhandledAlertException e) {
                try {
                    log.error("Unexpected alert occurred: {}", e.getAlertText());
                    // 여기에서 알림을 처리할 수 있습니다. 예를 들어, 알림을 닫습니다.
                    driver.switchTo().alert().accept();
                    // attempts를 증가시킨 후 현재 순서를 재시도합니다.
                    attempts++;
                } catch (NoAlertPresentException ex) {
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
