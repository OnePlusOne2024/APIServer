package org.spring.oneplusone.ServiceImpls;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ConvDTO;
import org.spring.oneplusone.Utils.Enums.URL;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GsConvCrawling implements Crawling{
    public List<ConvDTO> getConvList(){
        WebDriver driver = startingSession(URL.GSCONVLIST.getUrl());
        List<ConvDTO> result = new ArrayList<ConvDTO>();
        try{
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(40))
                    .pollingEvery(Duration.ofMillis(1))
                    .ignoring(NoSuchElementException.class);
            WebElement findMaxElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("next2")));
            //클릭
            findMaxElement.click();
			log.debug("MaxElement {}", findMaxElement);
            //blockUI waiting
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("blockUI")));
            WebElement blockUI = driver.findElement(By.className("blockUI"));
            wait.until(ExpectedConditions.stalenessOf(blockUI));

            //pagingTag안에 a 안에 class가 on인 것 찾기
            WebElement pageList =  wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pagingTagBox")));
            WebElement maxPageTag = pageList.findElement(By.className("on"));
            int maxPageNum = Integer.parseInt(maxPageTag.getText());
            WebElement convListTBody = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("storeInfoList")));
            List<WebElement> convList;
            Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
            ConvDTO convInfo;
			log.debug("최대 페이지 : {}", maxPageNum);
            for(int f = maxPageNum; f == 0 ; f--){
				log.debug("현재 페이지 : {}", f);
                convList = convListTBody.findElements(By.tagName("tr"));
                for(int z = 0; z== convList.size(); z++){
                    WebElement convName = convList.get(z).findElement(By.className("st_name"));
                    WebElement convAddress = convList.get(z).findElement(By.className("st_address"));
                    String convNameConvertToString = convName.getText();
                    String convAddressConvertToString = convAddress.getText();
                    String convCoordinate = convName.getAttribute("href");
                    Matcher matcher = pattern.matcher(convCoordinate);
                    double x = Double.parseDouble(matcher.group(2));
                    double y = Double.parseDouble(matcher.group(2));
                    convInfo = ConvDTO.builder()
                            .x(x)
                            .y(y)
                            .convAddr(convAddressConvertToString)
                            .convName(convNameConvertToString)
                            .build();
					log.debug("ConvName : {}, ConvAddr : {}, X : {}, Y : {}", convNameConvertToString, convAddressConvertToString, x, y);
                    result.add(convInfo);
                }
            }
            driver.quit();
        }catch(Exception e){
			log.debug("Error : {}", e);
            driver.quit();
        }
        return result;
    }
}
