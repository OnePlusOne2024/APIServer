package org.spring.oneplusone.ServiceImpls;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.Utils.Enums.ConvName;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Enums.URL;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ConvCrawling implements Crawling {
    @Autowired
    private CrawlingStatus crawlingStatus;

//    @Async
    public CompletableFuture<List<ConvDTO>> getConvList() {
        WebDriver driver = startingSession(URL.GSCONVLIST.getUrl());
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(40))
                    .pollingEvery(Duration.ofMillis(1))
                    .ignoring(NoSuchElementException.class);

            WebElement findMaxElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("next2")));
            findMaxElement.click();
            log.debug("MaxElement {}", findMaxElement);
            //gs Waiting
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blockUI")));
            WebElement blockUI = driver.findElement(By.className("blockUI"));
            wait.until(ExpectedConditions.stalenessOf(blockUI));

            WebElement pageList = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pagingTagBox")));
            WebElement maxPageTag = pageList.findElement(By.className("on"));
            int maxPageNum = Integer.parseInt(maxPageTag.getText());
            WebElement convListTBody = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("storeInfoList")));
            List<WebElement> convList;
            List<ConvDTO> result = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
            WebElement prevButton = pageList.findElement(By.className("prev"));
            ConvDTO convInfo;
            int undefineCoordinateConv = 0;//좌표가 제대로 연락되어 있지 않은 편의점
            log.info("GS 편의점 최대 페이지 : {}", maxPageNum);
            for (int f = maxPageNum; f > 0; f--) {
                if(f != maxPageNum){
                    prevButton = pageList.findElement(By.className("prev"));
                    prevButton.click();
                    //gs Waiting
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blockUI")));
                    blockUI = driver.findElement(By.className("blockUI"));
                    wait.until(ExpectedConditions.stalenessOf(blockUI));
                }
                log.debug("현재 페이지 : {}", f);
                convList = convListTBody.findElements(By.tagName("tr"));
                for (int z = 0; z < convList.size(); z++) {
                    WebElement convName = convList.get(z).findElement(By.className("st_name"));
                    WebElement convAddress = convList.get(z).findElement(By.className("st_address"));
                    String convNameConvertToString = convName.getText();
                    String convAddressConvertToString = convAddress.getText();
                    String convCoordinate = convName.getAttribute("href");
                    Matcher matcher = pattern.matcher(convCoordinate);
                    if (matcher.find()) {
                        String bracketContent = matcher.group(1);
                        // 괄호 안의 내용에서 숫자를 찾는 정규 표현식
                        String numberRegex = "([-+]?[0-9]*\\.?[0-9]+)";
                        Pattern numberPattern = Pattern.compile(numberRegex);
                        Matcher numberMatcher = numberPattern.matcher(bracketContent);
                        List<Double> coordinateList = new ArrayList<>();
                        while (numberMatcher.find()) {
                            // 매칭된 숫자를 double 타입으로 출력
                            coordinateList.add(Double.parseDouble(numberMatcher.group(1)));
                        }
                        if(coordinateList.size() == 0){//좌표값이 입력되지 않은 매장
                            undefineCoordinateConv +=1;
                            log.debug("좌표가 없는 편의점 : ",undefineCoordinateConv);
                            continue;
                        }
                        double x = coordinateList.get(0);
                        double y = coordinateList.get(1);
                        convInfo = ConvDTO.builder()
                                .x(x)
                                .y(y)
                                .convAddr(convAddressConvertToString)
                                .convName(convNameConvertToString)
                                .convBrandName(ConvName.GS25)
                                .build();
                        log.debug("ConvName : {}, ConvAddr : {}, X : {}, Y : {}", convNameConvertToString, convAddressConvertToString, x, y);
                        result.add(convInfo);
                    }
                }
            }
            log.info("좌표가 없는 편의점 총 갯수: ",undefineCoordinateConv);
            driver.quit();
            log.info("GS 편의점 크롤링 종료");
            return CompletableFuture.completedFuture(result);
        } catch (NoSuchElementException e) {
            driver.quit();
            crawlingStatus.stopCrawling("convenienceCrawling");
            log.error("Selenium Error : NoSuchElementException\n ", e);
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (WebDriverException e) {
            driver.quit();
            crawlingStatus.stopCrawling("convenienceCrawling");
            log.error("Selenium Error : WebDriverException \n", e);
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (Exception e) {
            driver.quit();
            crawlingStatus.stopCrawling("convenienceCrawling");
            log.error("에러 디테일 : ", e);
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }
    }
}
