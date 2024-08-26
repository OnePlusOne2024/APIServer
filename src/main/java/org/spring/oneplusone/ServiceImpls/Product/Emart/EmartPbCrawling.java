package org.spring.oneplusone.ServiceImpls.Product.Emart;

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
public class EmartPbCrawling implements Crawling {
    private final CrawlingStatus crawlingStatus;

    public EmartPbCrawling(CrawlingStatus crawlingStatus) {
        this.crawlingStatus = crawlingStatus;
    }

    public List<ProductDTO> getProductList() {
        try {
            log.info("EMART PB CRAWLING START");
            WebDriver driver = startingSession(URL.EMART_PB_URL.getUrl());//WebDriver 생성
            Wait<WebDriver> wait = new FluentWait<>(driver)//FluentWait는 유연한 시간 설정이 가능
                    .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                    .pollingEvery(Duration.ofMillis(1))//5초마다 재확인
                    .ignoring(org.openqa.selenium.NoSuchElementException.class);//Selenium 자체 에러 중 하나
            //변수 선언
            List<WebElement> prodList = new ArrayList<>();
            List<WebElement> resultBeforeProductDTO = new ArrayList<>();
            List<ProductDTO> result = new ArrayList<>();
            WebElement eventButton;
            WebElement nextButton;
            //ProductDTO 용 변수 선언
            ProductDTO productInfo;
            String productName;
            ConvName convName = ConvName.EMART;
            int productPrice;
            boolean productPB;
            //상품에 들어갈 행사 종류를 선언하고 초기값은 1+1, 이후 for문마다 다음 메뉴로 이동하고 값을 변경해줌
            String productEvent = "none";
            //7eleven은 카테고리 없음 => not found로
            String category = Category.NOT_FOUND.getCategoryName();
            String productImg;

            //페이지네이션에서 최대 없음 -> opacity가 0.3일 때 까지 for 문 돌리기
            //다음 버튼만 계속 누르기
            for(int i = 0; i < 2; i++){
                if(i == 1){
                    //fresh로 이동
                    List<WebElement> nextCategoryWrap =  wait.until(ExpectedConditions.presenceOfElementLocated(By.className("lngWrap"))).findElements(By.className("lnbMenu"));
                    nextCategoryWrap.get(nextCategoryWrap.size()-1).findElement(By.tagName("a")).click();
                    category = Category.FOOD.getCategoryName();
                    waitForJsExecutor(driver, wait, 0, 5);
                }
                while (true) {
                    //페이지에서 product List 읽어오기
                    prodList = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("mainContents"))).findElements(By.className("itemWrap"));
                    //product List 저장하기
                    resultBeforeProductDTO.addAll(prodList);
                    //다음버튼 활성화 되어있는지 확인하기 + 비활성화 시 break
                    if (isButtonClilckable(wait)) {//활성화시 false반환
                        break;
                    }
                    //되어 있으면 넘어가기 + 기다리기
                    nextButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("next")));
                    nextButton.click();
                    waitForJsExecutor(driver, wait, 0, 5);
                }
            }
            for (WebElement product : prodList) {
                productName = product.findElement(By.className("itemtitle")).findElement(By.tagName("p")).getText();
                try{
                    productPrice = Integer.parseInt(product.findElement(By.className("price")).getText().replace("원", "").replace(",", "").replace(" ", ""));
                }catch(NumberFormatException ex){
                    //가격이 제대로 적혀있지 않은 상품은 제거
                    continue;
                }
                productPB = true;//따로 구별 할 수 있는 정보X
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
                log.debug("[EMART][EVENT]상품명 : {}, 가격 : {}, 행사 종류 : {}, ", productName, productPrice, productEvent);
            }

            //driver 종료
            log.info("EMART PB CRAWLING FINISH");
            driver.quit();
            return result;

        }catch (org.openqa.selenium.NoSuchElementException e) {
            // Selenium의 NoSuchElementException을 커스텀 예외로 변환하여 throw
            log.error("[EMART][PB]Selenium Error : NoSuchElementException");
            log.error("detail : {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (WebDriverException e) {
            log.error("[EMART][PB]Selenium Error : WebDriverException");
            log.error("detail : {}", e);
            // Selenium의 일반적인 WebDriverException을 커스텀 예외로 변환하여 throw
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("[EMART][PB] {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }
    }

    private void waitForJsExecutor(WebDriver driver, Wait<WebDriver> wait, int attempts, int maxAttempts) {
        while (attempts < maxAttempts) {//네트워크 에러가 떳을 경우에 재시도 하기
            log.debug("[Emart]Execute jsExecutor");
            log.debug("[Emart]ajax가 완료되기 까지 기다리기");
            log.debug("[Emart]시도 횟수 : {}", attempts + 1);
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

    private boolean isButtonClilckable(Wait<WebDriver> wait) {
        try {
            WebElement next = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("next")));
            log.debug("[EMART][EVENT] + 버튼 있음 ");
            return !next.getAttribute("style").equals("opacity: 0.3;");//false
        }catch (org.openqa.selenium.NoSuchElementException e) {
            //style 옵션이 없음 = 아직 최종이 아님
            log.debug("[EMART][EVENT] + 버튼 없음 ");
            return true;
        }
    }
}
