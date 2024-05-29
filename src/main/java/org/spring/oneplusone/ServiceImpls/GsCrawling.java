package org.spring.oneplusone.ServiceImpls;


import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.Utils.Enums.ConvName;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Enums.URL;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class GsCrawling implements Crawling {

    private CrawlingStatus crawlingStatus;

    public GsCrawling(CrawlingStatus crawlingStatus){
        this.crawlingStatus = crawlingStatus;
    }
    public List<ProductDTO> getEventProduct() {
        log.debug("GS EVENT CRAWLING START");
        //enum에 선언된 url을 통해 크롤링 시도
        WebDriver driver = startingSession(URL.GS_EVENT_URL.getUrl());//WebDriver 생성
        //GS 행사 페이지에서 전체 목록 가져오기
        //driver에 대응하는 FluentWait객체를 생성
        try{
            Wait<WebDriver> wait = new FluentWait<>(driver)//FluentWait는 유연한 시간 설정이 가능
                    .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                    .pollingEvery(Duration.ofMillis(1))//5초마다 재확인
                    .ignoring(NoSuchElementException.class);//Selenium 자체 에러 중 하나
            WebElement productAllButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("TOTAL")));
            log.info("productAllButton 찾음 : " + productAllButton.getText());//로그로 수정
            productAllButton.click();//찾은 요소 클릭
        log.info("&&&&&&&&버튼 클릭 후 기다림&&&&&&&&&");//로그로 수정
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blockUI")));
            WebElement blockUI = driver.findElement(By.className("blockUI"));
            wait.until(ExpectedConditions.stalenessOf(blockUI));
            wait.until(ExpectedConditions.attributeContains(By.xpath("//*[@id='TOTAL']/.."), "class", "active"));
        log.info("버튼 클릭 완료");//로그로 수정
            //.tblwrap.mt50전부 찾기
            WebElement allProductPageBlock = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@id=\"wrap\"]/div[4]/div[2]/div[3]/div/div/div[4]")));
            //시작 페이지 paging
            WebElement pageNumList = allProductPageBlock.findElement(By.className("num"));
            //최대 페이지 찾기
            WebElement lastPageNum = allProductPageBlock.findElement(By.className("next2"));
        log.info("goLastPage 찾음");//로그로 수정
            String test = lastPageNum.getAttribute("onclick");
            //그냥 해당 버튼 누르지 말고 onclick의 text에서 값 뽑아내기
            //정규 표현식 사용
            //최대 페이지
            String extractedNumber = test.replaceAll("\\D+", "");//\\D : 숫자가 아닌 문자
        log.info(extractedNumber); // 출력: "206"
            int aa = Integer.valueOf(extractedNumber);
            log.info("최대 페이지 : " + aa);
            //페이지 번호 적혀 있는 리스트
            List<WebElement> pageList = pageNumList.findElements(By.cssSelector("[title='내용보기'"));
            log.debug("페이지 리스트 : " + pageList);
            //현재 페이지 이동 버튼
            //반복에 사용될 변수들 미리 선언 + 반복시에 재선언을 위해서
            WebElement goNextPageButton;    //다음 페이지 이동하는 element
            WebElement prod_list;           //다음
            List<WebElement> products;
            List<ProductDTO> crawlingResult = new ArrayList<>();
            ProductDTO currentProductInfo;
            //크롤링
            for (int currentPage = 1; currentPage < aa + 1; currentPage++) {//1page 부터 이후까지
                log.info(currentPage + "번째 페이지");//현재 페이지 출력
                //해당 페이지에 페이지가 로딩 될 때 까지 기다려야 됨
                if (currentPage != 1) {//현재 페이지가 1일 때는 이걸로
                    goNextPageButton = allProductPageBlock.findElement((By.className("next")));
                    goNextPageButton.click();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blockUI")));
                    blockUI = driver.findElement(By.className("blockUI"));
                    wait.until(ExpectedConditions.stalenessOf(blockUI));
                }
                //gs : tlbwrap mt50 class
                //9번째 태그가 전체 목록
                allProductPageBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[@id=\"wrap\"]/div[4]/div[2]/div[3]/div/div/div[4]")));
                prod_list = allProductPageBlock.findElement(By.className("prod_list"));
                products = prod_list.findElements(By.tagName("li"));
                for (WebElement product : products) {
                    String productName = product.findElement(By.className("tit")).getText();
                    ConvName convName = ConvName.GS25;
                    int productPrice = Integer.parseInt(product.findElement(By.className("cost")).getText().replace("원", "").replace(",", ""));
                    Boolean productPB = Boolean.FALSE;          //일단 pb false로 해놓고, 나중에 pb상품에서 수정
                    String productEvent = product.findElement(By.className("flg01")).findElement(By.tagName("span")).getText();
                    String category = "미정";
                    String productImg = product.findElement(By.tagName("img")).getAttribute("src");
                    currentProductInfo = ProductDTO.builder()
                            .name(productName)
                            .convname(convName)
                            .price(productPrice)
                            .pb(productPB)
                            .event(productEvent)
                            .category(category)
                            .image(productImg)
                            .build();
                    crawlingResult.add(currentProductInfo);
                }
            }
            //driver 종료
            log.info("GS EVENT CRAWLING FINISH");
            driver.quit();
            return crawlingResult;
        }catch (NoSuchElementException e) {
            driver.quit();
            crawlingStatus.stopCrawling("productCrawling");
            // Selenium의 NoSuchElementException을 커스텀 예외로 변환하여 throw
            log.error("Selenium Error : NoSuchElementException");
            log.error(e.getMessage());
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (WebDriverException e) {
            driver.quit();
            crawlingStatus.stopCrawling("productCrawling");
            log.error("Selenium Error : WebDriverException");
            log.error(e.getMessage());
            // Selenium의 일반적인 WebDriverException을 커스텀 예외로 변환하여 throw
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        }
        catch (Exception e) {
            driver.quit();
            crawlingStatus.stopCrawling("productCrawling");
            // 일반적인 예외 처리
            log.error(e.getMessage());
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }
    }
}
