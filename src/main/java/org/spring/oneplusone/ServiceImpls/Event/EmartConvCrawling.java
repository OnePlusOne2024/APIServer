package org.spring.oneplusone.ServiceImpls.Event;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.ServiceImpls.Crawling;
import org.spring.oneplusone.Utils.Enums.ConvName;
import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Enums.URL;
import org.spring.oneplusone.Utils.Error.CustomException;
import org.spring.oneplusone.Utils.Status.CrawlingStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component
public class EmartConvCrawling implements Crawling {

    private CrawlingStatus crawlingStatus;

    public EmartConvCrawling(CrawlingStatus crawlingStatus){this.crawlingStatus = crawlingStatus;}
    @Override
    public WebDriver startingSession(String webSitePath) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");//헤드리스로 열기
        // 위치 접근 권한 거절
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.geolocation", 2); // 1은 허용, 2는 차단
        options.setExperimentalOption("prefs", prefs);

        ChromeDriverService service = new ChromeDriverService.Builder()
                .withLogOutput(System.out) // 콘솔창에 로그 출력
                .build();

        ChromeDriver driver = new ChromeDriver(service, options);
        driver.get(webSitePath);

        synchronized (activeWebDrivers) {
            activeWebDrivers.add(driver);
        }

        return driver;
    }

    public List<ConvDTO> getConvList() {
        log.info("EMART 편의점 크롤링 시작");
        try {
            List<ConvDTO> result = new ArrayList<>();
            //검색 버튼 찾아서 누르기
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://emart24.co.kr/api1/store?page=1&search=&AREA1=&AREA2=&SVR_24=&SVR_AUTO=&SVR_PARCEL=&SVR_ATM=&SVR_WINE=&SVR_COFFEE=&SVR_SMOOTH=&SVR_APPLE=&SVR_TOTO=";
            //다음 버튼 찾기 & style의 수치가 0.3이 될 때 까지 확인하기
            ResponseEntity<String> response = restTemplate.exchange(url,HttpMethod.GET,null,String.class);
            JSONObject jsonObject = jsonObject = new JSONObject(response.getBody());
            String convCount = jsonObject.getString("count");
            //마지막 페이지 번호 찾기
            int lastPageNum = (Integer.parseInt(convCount) / 40) +1;
            log.info("[EMART]최대 페이지 번호 : {}", lastPageNum);
            JSONArray dataList;
            int wrongInfo = 0;
            for(int i =1 ; i<lastPageNum ; i++) {
                log.debug("[EMART]현재 페이지 : {}", i+1);
                url = "https://emart24.co.kr/api1/store?page=" + i +
                        "&search=&AREA1=&AREA2=&SVR_24=&SVR_AUTO=&SVR_PARCEL=&SVR_ATM=&SVR_WINE=&SVR_COFFEE=&SVR_SMOOTH=&SVR_APPLE=&SVR_TOTO=";
                // RestTemplate를 사용하여 외부 API 호출
                response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
                // JSONObject를 사용하여 JSON 응답 파싱
                jsonObject = new JSONObject(response.getBody());
                dataList = jsonObject.getJSONArray("data");
                ConvDTO convInfo;
                for (int z = 0; z < dataList.length(); z++) {
                    try {
                        JSONObject item = dataList.getJSONObject(z);
                        String convAddr = item.getString("ADDRESS");
                        String convName = item.getString("TITLE");
                        double longitude = Double.parseDouble(item.getString("LONGITUDE"));
                        double latitude = Double.parseDouble(item.getString("LATITUDE"));

                        // 좌표값이 잘못 입력되어 있는 경우를 처리
                        if (longitude > latitude) {
                            convInfo = ConvDTO.builder()
                                    .convAddr(convAddr)
                                    .convName(convName)
                                    .convBrandName(ConvName.EMART)
                                    .longitude(longitude)
                                    .latitude(latitude)
                                    .build();
                        } else {
                            convInfo = ConvDTO.builder()
                                    .convAddr(convAddr)
                                    .convName(convName)
                                    .convBrandName(ConvName.EMART)
                                    .longitude(latitude)
                                    .latitude(longitude)
                                    .build();
                        }
                        result.add(convInfo);
                        log.debug("[Emart] ConvName: {}, ConvAddr: {}, longitude: {}, latitude: {}", convName, convAddr, longitude, latitude);
                    } catch (JSONException ex) {
                        log.info("[EMART]{} 페이지 [{}]번째 편의점의 정보가 이상함: {}",i, z, ex.getMessage());
                        wrongInfo +=1;
                        // continue가 필요 없습니다. 예외를 처리한 후 자동으로 다음 반복으로 넘어갑니다.
                    } catch (NumberFormatException ex) {
                        log.info("[EMART]{} 페이지 [{}]번째 편의점의 좌표값 형식이 잘못됨: {}",i, z, ex.getMessage());
                        wrongInfo +=1;
                        // NumberFormatException도 개별적으로 처리합니다.
                    }
                }
            }
            log.info("EMART 편의점 크롤링 종료 ");
            log.info("[갯수 : {}, 정보가 손상된 편의점 갯수 : {}]", result.size(), wrongInfo);
                return result;
        } catch (
                NoSuchElementException e) {
            log.error("[EMART]Selenium Error : NoSuchElementException\n ", e);
            log.error("시간 : {}", LocalDateTime.now());
            log.error("발생위치 : {}", e.getStackTrace());
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("convenienceCrawling");
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (
                WebDriverException e) {
            log.error("[EMART]Selenium Error : WebDriverException \n", e);
            log.error("시간 : {}", LocalDateTime.now());
            log.error("발생위치 : {}", e.getStackTrace());
            crawlingStatus.stopCrawling("convenienceCrawling");
            this.stopAllCrawling();
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (
                Exception e) {
            log.error("[EMART]에러 디테일 : ", e);
            log.error("시간 : {}", LocalDateTime.now());
            log.error("발생위치 : {}", e.getStackTrace());
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("convenienceCrawling");
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }
    }

    private void waitForFinishCiteLoading(WebDriver driver, Wait<WebDriver> wait, int attempts, int maxAttempts) {
        while (attempts < maxAttempts) {//네트워크 에러가 떳을 경우에 재시도 하기
            log.debug("[EMART]Execute jsExecutor : 현재 실행중인 Jquery 확인");
            log.debug("[EMART]ajax가 완료되기 까지 기다리기");
            log.debug("[EMART]시도 횟수 : {}", attempts + 1);
            try {
                wait.until((WebDriver d) -> {
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) d;
                    return (Boolean) jsExecutor.executeScript("return jQuery.active == 0;");
                });
                break;
            } catch (UnhandledAlertException e) {
                try {
                    log.error("시간 : ", LocalDateTime.now());
                    log.error("[waitForFinishCiteLoading]Unexpected alert occurred: {}", e.getAlertText());
                    // 여기에서 알림을 처리할 수 있습니다. 예를 들어, 알림을 닫습니다.
                    driver.switchTo().alert().accept();
                    // attempts를 증가시킨 후 현재 순서를 재시도합니다.
                    attempts++;
                } catch (NoAlertPresentException ex) {
                    log.error("시간 : ", LocalDateTime.now());
                    log.error("[waitForFinishCiteLoading]No alert present: {}", ex.getMessage());
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