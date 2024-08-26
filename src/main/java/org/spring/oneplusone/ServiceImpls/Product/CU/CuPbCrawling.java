package org.spring.oneplusone.ServiceImpls.Product.CU;

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
public class CuPbCrawling implements Crawling {
    private final CrawlingStatus crawlingStatus;
    public CuPbCrawling(CrawlingStatus crawlingStatus){
        this.crawlingStatus = crawlingStatus;
    }
    public List<ProductDTO> getProductList(){
        try{
            log.info("CU PB CRAWLING START");
            WebDriver driver = startingSession(URL.CU_PB_URL.getUrl());//WebDriver 생성
            Wait<WebDriver> wait = new FluentWait<>(driver)//FluentWait는 유연한 시간 설정이 가능
                    .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                    .pollingEvery(Duration.ofMillis(1))//5초마다 재확인
                    .ignoring(org.openqa.selenium.NoSuchElementException.class);//Selenium 자체 에러 중 하나
            //PB 상품과 단독 운영 상품
            //변수 미리 선언
            WebElement eventButton;
            WebElement prodListBtn;
            List<WebElement> prodList;
            List<ProductDTO> result = new ArrayList<>();
            //ProductDTO 용 변수 선언
            ProductDTO productInfo;
            String productName;
            ConvName convName = ConvName.CU;
            int productPrice;
            boolean productPB;
            //상품에 들어갈 행사 종류를 선언하고 초기값은 1+1, 이후 for문마다 다음 메뉴로 이동하고 값을 변경해줌
            String productEvent = "none";
            String productEventNone = "none";
            String productEventOnePlOne = "1+1";
            String productEventTwoPlTwo = "2+1";
            //7eleven은 카테고리 없음 => not found로
            String category = Category.NOT_FOUND.getCategoryName();
            String productImg;
            int lastButtonClickNum = 1;
            for(int i = 0; i < 2; i++){
                if(i == 1){
                    //1+1
                    eventButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cardInfo_02")));
                    eventButton.findElement(By.tagName("a")).click();
                    //기다리기 블록
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("AjaxLoading")));
                }
                while (true) {//버튼이 없을 때 까지 계속 버튼 누르기
                    try {
                        prodListBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("prodListBtn")));
                        log.debug("[CU][PB] + 버튼 있음 ");
                        prodListBtn.click();
                        //기다리기 블록
                        log.info("[CU][PB] {} 번 누름", lastButtonClickNum);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("AjaxLoading")));
                    }catch (org.openqa.selenium.StaleElementReferenceException e) {
                        log.debug("[CU][PB] + 버튼 없음 ");
                        break;
                    }
                }
                //결과 찾기
                prodList = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("prodListWrap"))).findElements(By.className("prod_list"));
                for(WebElement product : prodList){
                    productName = product.findElement(By.className("name")).findElement(By.tagName("p")).getText();
                    productPrice = Integer.parseInt(product.findElement(By.tagName("strong")).getText().replace(",",""));
                    productPB = true;
                    WebElement findBadge = null;
                    try{
                        //뱃지에 따라서 1+1, 2+1으로 변경
                        findBadge = product.findElement(By.className("badge"));
                        if(findBadge.findElement(By.tagName("span")).getText().equals("1+1")){
                            productEvent = productEventOnePlOne;
                        }else if(findBadge.findElement(By.tagName("span")).getText().equals("2+1")){
                            productEvent = productEventTwoPlTwo;
                        }
                    }catch (org.openqa.selenium.NoSuchElementException e) {
                        // 예외 발생 시 none으로 설정
                        productEvent = productEventNone;
                    }
                    productImg = product.findElement(By.tagName("img")).getAttribute("src");
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
                    log.debug("[CU][PB]상품명 : {}, 가격 : {}, 행사 종류 : {}, ", productName, productPrice, productEvent);
                }
            }
            //driver 종료
            log.info("CU PB CRAWLING FINISH");
            driver.quit();
            return result;
        }catch (org.openqa.selenium.NoSuchElementException e) {
            // Selenium의 NoSuchElementException을 커스텀 예외로 변환하여 throw
            log.error("[CU][PB]Selenium Error : NoSuchElementException");
            log.error("detail : {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (WebDriverException e) {
            log.error("[CU][PB]Selenium Error : WebDriverException");
            log.error("detail : {}", e);
            // Selenium의 일반적인 WebDriverException을 커스텀 예외로 변환하여 throw
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("[CU][PB] {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }
    }
    private void waitForJsExecutor(WebDriver driver, Wait<WebDriver> wait, int attempts, int maxAttempts) {
        while (attempts < maxAttempts) {//네트워크 에러가 떳을 경우에 재시도 하기
            log.debug("[CU]Execute jsExecutor");
            log.debug("[CU]ajax가 완료되기 까지 기다리기");
            log.debug("[CU]시도 횟수 : {}", attempts+1);
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
    private boolean isProductListBtnDeleted(Wait<WebDriver> wait) {
        try {
            WebElement prodListBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("prodListBtn")));
            log.debug("[CU][PB] + 버튼 있음 ");
            return !prodListBtn.isDisplayed();
        }catch (org.openqa.selenium.NoSuchElementException e) {
            log.debug("[CU][PB] + 버튼 없음 ");
            return true;
        }
    }
}
