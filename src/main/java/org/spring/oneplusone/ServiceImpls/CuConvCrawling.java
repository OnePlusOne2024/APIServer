package org.spring.oneplusone.ServiceImpls;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.openqa.selenium.*;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.json.Json;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CuConvCrawling implements Crawling {
    private CrawlingStatus crawlingStatus;

    public CuConvCrawling(CrawlingStatus crawlingStatus) {
        this.crawlingStatus = crawlingStatus;
    }

    public List<ConvDTO> getConvList() {
        log.info("CU 편의점 크롤링 시작");
        WebDriver driver = startingSession(URL.CU_CONV_URL.getUrl());
        try {
            FluentWait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(40))
                    .pollingEvery(Duration.ofMillis(1))
                    .ignoring(NoSuchElementException.class);

            //시,도 select block 선택 및 option 길이 측정
            WebElement siDoWebElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("sido")));
            Select siDoSelect = new Select(siDoWebElement);
            List<WebElement> siDoOptions = siDoSelect.getOptions();
            //시, 도 value 값 변수 선언
            String currentSiDoName;
            //구,군 select, option 변수 선언
            WebElement guGunWebElement;
            Select guGunSelect;
            List<WebElement> guGunOptions;
            String currentGuGunName;
            //동 select, option 변수 선언
            WebElement dongWebElement;
            Select dongSelect = null;
            List<WebElement> dongOptions;
            String currentDongName;
            //결과 반환용 List
            List<ConvDTO> result = new ArrayList<>();
            //검색 버튼 찾기
            WebElement searchWrap;
            WebElement searchButtonWrap;
            WebElement searchButton;
            //result_store 찾기 용 변수 선언
            WebElement resultStore;
            //매장 리스트가 나와있는 table 변수 선언
            WebElement convTable;
            //매장 리스트
            List<WebElement> convListTr;
            //매장 이름과 주소가 저장될 List 선언
            List<WebElement> convInfoTd;
            //등록된 게시물이 없는지 확인하는 용도로 WebElement 선언
            WebElement checkDataInTbody;
            //편의점 정보용 변수선언;
            String convName;
            String convAddr;
            WebElement convAddrWebElement;
            String convAddrOnclick;
            String convAddrText;
            List<Double> coordinate;
            double longitude;
            double latitude;
            ConvName convBrandName = ConvName.CU;
            ConvDTO convInfo;
            //시,도 만큼 반복
            for (int sidoCurrentOption = 1; sidoCurrentOption < siDoOptions.size(); sidoCurrentOption++) {
                //현재의 시,도를 선택하고 기다림
                currentSiDoName = siDoOptions.get(sidoCurrentOption).getAttribute("value");
                siDoSelect.selectByValue(currentSiDoName);
                log.info("[CU]현재 시,도 : {}", currentSiDoName);
                waitForFinishCiteLoading(driver, wait, 0, 5);
                //바뀐 구,군을 가져옴
                guGunWebElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("Gugun")));
                guGunSelect = new Select(guGunWebElement);
                guGunOptions = guGunSelect.getOptions();
                for (int guGunCurrentOption = 1; guGunCurrentOption < guGunOptions.size(); guGunCurrentOption++) {
                    //현재 구,군을 선택하고 기다림
                    currentGuGunName = guGunOptions.get(guGunCurrentOption).getAttribute("value");
                    guGunSelect.selectByValue(currentGuGunName);
                    log.info("[CU]현재 구,군 : {}", currentGuGunName);
                    waitForFinishCiteLoading(driver, wait, 0, 5);
                    //바뀐 동을 가져옴
                    if(currentSiDoName.equals("세종특별자치시")){
                        dongOptions = new ArrayList<>(2);
                    }else {
                        dongWebElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Dong")));
                        dongSelect = new Select(dongWebElement);
                        dongOptions = dongSelect.getOptions();
                    }
                    for (int dongCurrentOption = 1; dongCurrentOption < dongOptions.size(); dongCurrentOption++) {
                        //현재 동을 선택하고 기다림
                        if(!currentSiDoName.equals("세종특별자치시")){
                            currentDongName = dongOptions.get(dongCurrentOption).getAttribute("value");
                            dongSelect.selectByValue(currentDongName);
                            log.info("[CU]현재 동 : {}", currentDongName);
                        }
                        waitForFinishCiteLoading(driver, wait, 0, 5);
                        //검색 누르고 기다림
                        searchWrap = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("search_wrap")));
                        searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@alt='검색']")));
//                        searchButton.click();;
                        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", searchButton);
                        waitForFinishCiteLoading(driver, wait, 0, 5);
                        //로딩된 결과창을 새로 가져옴
                        resultStore = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("result_store")));
                        //해당 결과창에서 table 탐색
                        convTable = resultStore.findElement(By.tagName("tbody"));
                        //등록된 게시물이 없는지 확인하기
                        checkDataInTbody = convTable.findElement(By.tagName("td"));
                        //일치 하지 않을 경우
                        if (!checkDataInTbody.getText().equals("등록된 게시물이 없습니다.")) {
                            convListTr = convTable.findElements(By.tagName("tr"));
                            for (int currentConv = 0; currentConv < convListTr.size(); currentConv++) {
                                try{
                                    convInfoTd = convListTr.get(currentConv).findElements(By.tagName("td"));
                                    convName = convInfoTd.get(0).findElement(By.className("name")).getText();
                                    convAddrWebElement = convInfoTd.get(1).findElement(By.tagName("a"));
                                    convAddr = convAddrWebElement.getText();
                                    convAddrOnclick = convAddrWebElement.getAttribute("onclick");
                                    // 정규 표현식을 사용하여 주소 부분을 추출합니다.
                                    Pattern pattern = Pattern.compile("searchLatLng\\('([^']*)',");
                                    Matcher matcher = pattern.matcher(convAddrOnclick);
                                    if (matcher.find()) {
                                        convAddrText = matcher.group(1); // 첫 번째 그룹에 해당하는 주소를 가져옵니다.
                                    } else {
                                        convAddrText = "주소를 찾을 수 없습니다.";
                                    }
                                    //주소가 없을 경우 length = 0
                                    coordinate = waitForGetCoordinate(convAddrText);
                                    if (coordinate.size() != 0) {//사이즈가 0이면 다음으로 넘어감
                                        longitude = coordinate.get(0);
                                        latitude = coordinate.get(1);
                                        convInfo = ConvDTO.builder()
                                                .convAddr(convAddr)
                                                .convBrandName(convBrandName)
                                                .convName(convName)
                                                .latitude(latitude)
                                                .longitude(longitude)
                                                .convBrandName(ConvName.CU)
                                                .build();
                                        log.debug("[CU]ConvName : {}, ConvAddr : {}, longitude : {}, latitude : {}", convName, convAddr, longitude, latitude);
                                        result.add(convInfo);
                                    }
                                }catch (ResourceAccessException cunnection){
                                    driver = startingSession(URL.CU_CONV_URL.getUrl());
                                    guGunWebElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("Gugun")));
                                    guGunSelect = new Select(guGunWebElement);
                                    guGunOptions = guGunSelect.getOptions();
                                    resultStore = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("result_store")));
                                    searchButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@alt='검색']")));
                                    dongWebElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Dong")));
                                    dongSelect = new Select(dongWebElement);
                                    dongOptions = dongSelect.getOptions();
                                    siDoWebElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("sido")));
                                    siDoSelect = new Select(siDoWebElement);
                                    siDoOptions = siDoSelect.getOptions();
                                }
                            }
                        }
                        //일치할 경우 다음으로 넘어감
                    }
                }
            }

            log.info("CU 편의점 크롤링 종료 ");
            log.info("[CU]갯수 : {}", result.size());
            driver.quit();
            return result;
        } catch (
                NoSuchElementException e) {
            log.error("[CU]Selenium Error : NoSuchElementException\n ", e);
            log.error("시간 : {}", LocalDateTime.now());
            log.error("발생위치 : {}", e.getStackTrace());
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("convenienceCrawling");
            throw new CustomException(ErrorList.CRAWLING_SELENIUM);
        } catch (
                WebDriverException e) {
            log.error("[CU]Selenium Error : WebDriverException \n", e);
            log.error("시간 : {}", LocalDateTime.now());
            log.error("발생위치 : {}", e.getStackTrace());
            crawlingStatus.stopCrawling("convenienceCrawling");
            this.stopAllCrawling();
            throw new CustomException(ErrorList.CRAWLING_WEB_ELEMENT);
        } catch (
                Exception e) {
            log.error("[CU]에러 디테일 : ", e);
            log.error("시간 : {}", LocalDateTime.now());
            log.error("발생위치 : {}", e.getStackTrace());
            this.stopAllCrawling();
            crawlingStatus.stopCrawling("convenienceCrawling");
            throw new CustomException(ErrorList.CRAWLING_UNEXPECTED_ERROR);
        }
    }

    private void waitForFinishCiteLoading(WebDriver driver, Wait<WebDriver> wait, int attempts, int maxAttempts) {
        while (attempts < maxAttempts) {//네트워크 에러가 떳을 경우에 재시도 하기
            log.debug("[CU]Execute jsExecutor : 현재 실행중인 Jquery 확인");
            log.debug("[CU]ajax가 완료되기 까지 기다리기");
            log.debug("[CU]시도 횟수 : {}", attempts + 1);
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

    public List<Double> waitForGetCoordinate(String convAddr) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + convAddr;

        // HttpHeaders 객체 생성 및 Authorization 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK 7adae4b986e9a03ba42d843fdb1944fe");

        // HttpEntity 객체 생성 (헤더 포함)
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // RestTemplate를 사용하여 외부 API 호출
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // JSONObject를 사용하여 JSON 응답 파싱
        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray documents = jsonObject.getJSONArray("documents");
        List<Double> coordinate = new ArrayList<>();
        if (documents.length() > 0) {
            JSONObject firstDocument = documents.getJSONObject(0);
            String x = firstDocument.getString("x");
            String y = firstDocument.getString("y");
            if (Double.parseDouble(x) > Double.parseDouble(y)) {
                coordinate.add(Double.parseDouble(x));
                coordinate.add(Double.parseDouble(y));
            } else {
                coordinate.add(Double.parseDouble(y));
                coordinate.add(Double.parseDouble(x));
            }
        }
        log.debug("document의 길이 : {}", documents.length());
        return coordinate;
    }
}
