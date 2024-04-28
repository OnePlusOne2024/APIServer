package org.spring.oneplusone.ServiceImpls;


import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchContextException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.spring.oneplusone.DTO.ProductDTO;
import org.spring.oneplusone.Global.Enums.URL;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class GsCrawling implements Crawling{
    public List<ProductDTO> getEventProduct() {
        //enum에 선언된 url을 통해 크롤링 시도
        WebDriver driver = startingSession(URL.GSEVENTURL.getUrl());

        //GS 행사 페이지에서 전체 목록 가져오기
        //driver에 대응하는 FluentWait객체를 생성
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)//FluentWait는 유연한 시간 설정이 가능
                .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                .pollingEvery(Duration.ofSeconds(5))//5초마다 재확인
                .ignoring(NoSuchContextException.class);//Selenium 자체 에러 중 하나
        //객체 생성시 설정한 timeout은 이후 해당 객체 호출시에도 적용
        WebElement lastPageButton = wait.until(ExpectedConditions
                .visibilityOfElementLocated(//해당 위치의 객체가 생성된느지
                        By.xpath(
                                "/html/body/div[1]/div[4]/div[2]/div[3]/div/div/ul/li[4]/span/a"
                        )
                )
        );
        System.out.println("lastPageButton 찾음");//로그로 수정
        lastPageButton.click();//찾은 요소 클릭
        //최대 페이지 찾기
        WebElement goLastPage = wait.until(
                ExpectedConditions
                        .visibilityOfElementLocated(
                                By.xpath("/html/body/div[1]/div[4]/div[2]/div[3]/div/div/div[4]/div/a[4]")
                        )
        );
        System.out.println("goLastPage 찾음");//로그로 수정
        System.out.println(goLastPage.getAttribute("onclick"));
        goLastPage.click();
        //최대 페이지
        WebElement findATagElementsAtLastPage = wait.until(
                ExpectedConditions
                        .visibilityOfElementLocated(
                                By.xpath("/html/body/div[1]/div[4]/div[2]/div[3]/div/div/div[4]/div/span")

                        )
        );
//        System.out.println(findATagElementsAtLastPage.getText());
//        int lastPageNum = Integer.parseInt(Arrays.asList(findATagElementsAtLastPage
//                .getText()
//                .split("|")
//                ).get()
//        );
        List<String> lastPageList = Arrays.asList(
                findATagElementsAtLastPage
                        .getText()
                        .split("|")
        );
        int lastPageNum = Integer.parseInt(lastPageList.get(lastPageList.size()-1));
        System.out.println(lastPageNum);//로그로 변환



        return null;
    }
}
