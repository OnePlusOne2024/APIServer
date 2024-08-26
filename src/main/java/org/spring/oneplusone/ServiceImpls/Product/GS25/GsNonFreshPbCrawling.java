package org.spring.oneplusone.ServiceImpls.Product.GS25;


import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
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
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class GsNonFreshPbCrawling implements Crawling {
    private CrawlingStatus crawlingStatus;

    public GsNonFreshPbCrawling(CrawlingStatus crawlingStatus) {
        this.crawlingStatus = crawlingStatus;
    }

    public List<ProductDTO> getProductList() {
        log.info("GS NON FRESH PB CRAWLING START");
        //도시락, 김밥/주먹밥, 햄버거/샌드위치, 간편식 -> 전부 식품
        WebDriver driver = startingSession(URL.GS_PB_NOT_FRESH_URL.getUrl());//WebDriver 생성}
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)//FluentWait는 유연한 시간 설정이 가능
                    .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                    .pollingEvery(Duration.ofMillis(1))//5초마다 재확인
                    .ignoring(org.openqa.selenium.NoSuchElementException.class);//Selenium 자체 에러 중 하나

            //상품 카테고리들을 선택한다.
            WebElement drinkWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productDrink")));
            WebElement milkWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productMilk")));
            WebElement cookieWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productCookie")));
            WebElement ramenWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productRamen")));
            WebElement goodsWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("productGoods")));
            //선택한 카테고리들을 list에 추가한다.
            List<Pair<WebElement, String>> categoryList = new ArrayList<>(Arrays.asList(
                    new Pair<>(drinkWebElement, Category.DRINK.getCategoryName()),
                    new Pair<>(milkWebElement, Category.DRINK.getCategoryName()),
                    new Pair<>(cookieWebElement, Category.SNACK.getCategoryName()),
                    new Pair<>(ramenWebElement, Category.FOOD.getCategoryName()),
                    new Pair<>(goodsWebElement, Category.LIVING_PRODUCT.getCategoryName())
            ));
            //변수 선언
            WebElement blockUI;
            List<ProductDTO> result = new ArrayList<>();
            WebElement goNextPageButton;    //다음 페이지 이동하는 element
            for(Pair<WebElement,String> categoryOption : categoryList) {
                categoryOption.a.click();
                //기다리기
                blockUI = driver.findElement(By.className("blockUI"));
                log.info("[GS25][NON_FRESH_PB]현재 카테고리 : {}", categoryOption.b);
                wait.until(ExpectedConditions.stalenessOf(blockUI));
                //최대 페이지 번호 찾기
                WebElement lastPageNum = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("next2")));
                String lastPageText = lastPageNum.getAttribute("onclick");
                // \\D : 숫자가 아닌 문자
                int extractedNumber = Integer.valueOf(lastPageText.replaceAll("\\D+", ""));
                //ProductDTO 용 변수 선언
                ProductDTO productInfo;
                String productName;
                ConvName convName = ConvName.GS25;
                int productPrice;
                boolean productPB;
                String productEvent;
                String category = categoryOption.b;
                String productImg;
                //page 만큼 for문 돌리기
                for (int currentPage = 1; currentPage < extractedNumber + 1; currentPage++) {
                    //물품 리스트 찾기
                    WebElement prodListWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("prod_list")));
                    List<WebElement> prodLists = prodListWebElement.findElements(By.tagName("li"));
                    for (WebElement product : prodLists) {
                        productName = product.findElement(By.className("tit")).getText();
                        productPrice = Integer.parseInt(product.findElement(By.className("cost")).getText().replace("원", "").replace(",", ""));
                        productPB = true;
                        productEvent = "none";
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
                        result.add(productInfo);
                        log.debug("[GS25][NON_FRESH_PB]상품명 : {}, 가격 : {}, 행사 종류 : {}, ", productName, productPrice, productEvent);
                    }
                    if (currentPage != extractedNumber) {//최대 페이지일 때는 마지막 버튼이 없으므로 누르지 않음
                        goNextPageButton = wait.until(ExpectedConditions.presenceOfElementLocated((By.className("next"))));
                        goNextPageButton.click();
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blockUI")));
                        blockUI = driver.findElement(By.className("blockUI"));
                        wait.until(ExpectedConditions.stalenessOf(blockUI));
                    }
                }
            }
            log.info("GS NON FRESH PB CRAWLING FINISH");
            driver.quit();
            return result;
        }catch (org.openqa.selenium.NoSuchElementException e) {
            // Selenium의 NoSuchElementException을 커스텀 예외로 변환하여 throw
            log.error("[GS25][NON_FRESH_PB]Selenium Error : NoSuchElementException");
            log.error("detail : {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (WebDriverException e) {
            log.error("[GS25][NON_FRESH_PB]Selenium Error : WebDriverException");
            log.error("detail : {}", e);
            // Selenium의 일반적인 WebDriverException을 커스텀 예외로 변환하여 throw
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (Exception e) {
            // 일반적인 예외 처리
            log.error("[GS25][NON_FRESH_PB] {}", e);
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("productCrawling");
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }

    }
}
