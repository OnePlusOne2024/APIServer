package org.spring.oneplusone.ServiceImpls;


import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.Utils.Enums.ConvName;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Enums.URL;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SevenConvCrawling implements Crawling {

    private CrawlingStatus crawlingStatus;

    public SevenConvCrawling(CrawlingStatus crawlingStatus){
        this.crawlingStatus = crawlingStatus;
    }

    public List<ConvDTO> getConvList() {
            log.info("SevenEleven 편의점 크롤링 시작");
            WebDriver driver = startingSession(URL.SEVEN_ELEVEN_CONV_LIST.getUrl());
            try {
                Wait<WebDriver> wait = new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(40))
                        .pollingEvery(Duration.ofMillis(1))
                        .ignoring(NoSuchElementException.class);
                WebElement findStoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".util_store.store_open")));
                findStoreButton.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("layer_pop_wrap")));
                WebElement storeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("storeForm")));
                WebElement siDoList = storeElement.findElement(By.name("storeLaySido"));
                WebElement guList;
                WebElement storeCheckButton = storeElement.findElement(By.id("storeButton1"));
                Select siDoSelect = new Select(siDoList);
                Select guSelect;
                List<WebElement> siDoOptions = siDoSelect.getOptions();
                List<WebElement> guOptions;
                List<WebElement> storeLists;
                WebElement storeListWebElement;
                List<WebElement> spanListInListStore;
                List<ConvDTO> result = new ArrayList<>();
                String siDoValue;
                String guValue;
                String convName;
                String convAddr;
                WebElement storeAddress;
                double longitude;
                double latitude;
                List<Double> coordinate = new ArrayList<>();
                Pattern pattern = Pattern.compile("markerClick\\(\\d+,(\\d+\\.\\d+),(\\d+\\.\\d+)\\)");
                Matcher matcher;
                ConvDTO convInfo;
                //시,도 for문
                for (int z = 1; z < siDoOptions.size(); z++) {
                    //구 안에서 for문
                    storeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("storeForm")));
                    siDoList = storeElement.findElement(By.name("storeLaySido"));
                    //select 다시 찾기
                    siDoSelect = new Select(siDoList);
                    siDoOptions = siDoSelect.getOptions();
                    siDoValue = siDoOptions.get(z).getAttribute("value");
                    //해당 값의 select로 진행하기
                    siDoSelect.selectByValue(siDoValue);
                    log.info("[Seven Eleven]시/도 : " + siDoValue);
                    int attemptsSiDo = 0;
                    int maxAttemptsSiDo = 5;
                    waitForJsExecutor(driver, wait, attemptsSiDo, maxAttemptsSiDo);
                    storeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("storeForm")));
                    guList = storeElement.findElement(By.name("storeLayGu"));
                    guSelect = new Select(guList);
                    guOptions = guSelect.getOptions();
                    for (int i = 1; i < guOptions.size(); i++) {
                        int attemptsGu = 0;
                        int maxAttemptsGu = 5; // 최대 시도 횟수를 정의합니다.

                        guValue = guOptions.get(i).getAttribute("value");
                        guSelect.selectByValue(guValue);
                        log.info("[Seven Eleven]구 : " + guValue);
                        storeCheckButton.click();
                        waitForJsExecutor(driver, wait, attemptsGu, maxAttemptsGu);
                        storeListWebElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("list_stroe")));
                        storeLists = storeListWebElement.findElements(By.tagName("li"));
                        if (storeLists.get(storeLists.size()-1).getAttribute("class").equals("no_data")) {
                            log.debug("[Seven Eleven] 검색 결과가 없습니다 : {}", storeLists.get(storeLists.size()-1).getAttribute("class"));
                        }else
                        {
                            for (WebElement storeList : storeLists) {
                                spanListInListStore = storeList.findElements(By.tagName("span"));
                                convName = spanListInListStore.get(0).getText();
                                convAddr = spanListInListStore.get(1).getText();
                                storeAddress = storeList.findElement(By.tagName("a"));
                                String addressHref = storeAddress.getAttribute("href");
                                matcher = pattern.matcher(addressHref);
                                if (matcher.find()) {
                                    coordinate.add(Double.parseDouble(matcher.group(1)));
                                    coordinate.add(Double.parseDouble(matcher.group(2)));
                                    if(coordinate.get(0) > coordinate.get(1)){
                                        longitude = coordinate.get(0);
                                        latitude = coordinate.get(1);
                                    }else{
                                        latitude = coordinate.get(0);
                                        longitude = coordinate.get(1);
                                    }

                                    convInfo = ConvDTO.builder()
                                            .longitude(longitude)
                                            .latitude(latitude)
                                            .convAddr(convName)
                                            .convName(convAddr)
                                            .convBrandName(ConvName.SEVENELEVEN)
                                            .build();
                                    log.debug("[Seven Eleven]ConvName : {}, ConvAddr : {}, longitude : {}, latitude : {}", convName, convAddr, longitude, latitude);
                                    result.add(convInfo);
                                }
                            }
                        }
                        //확인을 누르고 나서 객체가 초기화 되기 때문에 새로 찾아야함
                        storeElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("storeForm")));
                        guList = storeElement.findElement(By.name("storeLayGu"));
                        guSelect = new Select(guList);
                        guOptions = guSelect.getOptions();
                        storeCheckButton = storeElement.findElement(By.id("storeButton1"));
                    }

                }
                driver.quit();
                log.info("SevenEleven 편의점 크롤링 종료");
                return result;
            } catch (
                    NoSuchElementException e) {
                log.error("[SevenEleven]Selenium Error : NoSuchElementException\n ", e);
                log.error("시간 : {}", LocalDateTime.now());
                log.error("발생위치 : {}",e.getStackTrace());
//                driver.quit();
                this.stopAllCrawling();
                crawlingStatus.stopCrawling("convenienceCrawling");
                throw new CustomException(ErrorList.CRAWLING_SELENIUM);
            } catch (
                    WebDriverException e) {
                log.error("[SevenEleven]Selenium Error : WebDriverException \n", e);
                log.error("시간 : {}", LocalDateTime.now());
                log.error("발생위치 : {}",e.getStackTrace());
                this.stopAllCrawling();
//                driver.quit();
                crawlingStatus.stopCrawling("convenienceCrawling");
                this.stopAllCrawling();
                throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
            } catch (
                    Exception e) {
                log.error("[SevenEleven]에러 디테일 : ", e);
                log.error("시간 : {}", LocalDateTime.now());
                log.error("발생위치 : {}",e.getStackTrace());
//                driver.quit();
                this.stopAllCrawling();
                crawlingStatus.stopCrawling("convenienceCrawling");
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
}
