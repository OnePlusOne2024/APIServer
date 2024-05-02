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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GsCrawling implements Crawling{
    public List<ProductDTO> getEventProduct() {
        //enum에 선언된 url을 통해 크롤링 시도
        WebDriver driver = startingSession(URL.GSEVENTURL.getUrl());
        //GS 행사 페이지에서 전체 목록 가져오기
        //driver에 대응하는 FluentWait객체를 생성
        Wait<WebDriver> wait = new FluentWait<>(driver)//FluentWait는 유연한 시간 설정이 가능
                .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                .pollingEvery(Duration.ofSeconds(10))//5초마다 재확인
                .ignoring(NoSuchContextException.class);//Selenium 자체 에러 중 하나
        //객체 생성시 설정한 timeout은 이후 해당 객체 호출시에도 적용
        WebElement productAllButton = wait.until(ExpectedConditions
                .visibilityOfElementLocated(//해당 위치의 객체가 생성된느지
                        By.xpath(
                                "/html/body/div[1]/div[4]/div[2]/div[3]/div/div/ul/li[4]/span"
                        )
                )
        );
        System.out.println("productAllButton 찾음 : " + productAllButton.getText());//로그로 수정
        productAllButton.click();//찾은 요소 클릭
        System.out.println("&&&&&&&&버튼 클릭 후 기다림&&&&&&&&&");//로그로 수정
        wait.until(ExpectedConditions.attributeContains(productAllButton, "class", "active") );//버튼이 실제로 눌리기 까지 기다림(wait사용)
        System.out.println("버튼 클릭 완료");//로그로 수정
        //이동 후 wait 재할당
        //나중에 메모리 관리 처리
        wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(40))//40초까지 기다림
                .pollingEvery(Duration.ofSeconds(10))//5초마다 재확인
                .ignoring(NoSuchContextException.class);//Selenium 자체 에러 중 하나
        //최대 페이지 찾기
        WebElement lastPageNum = wait.until(
                ExpectedConditions
                        .visibilityOfElementLocated(
                                By.cssSelector("#wrap > div.cntwrap > div.cnt > div.cnt_section.mt50 > div > div > div:nth-child(9) > div > a.next2")
                        )
        );
        System.out.println("goLastPage 찾음");//로그로 수정
        System.out.println(lastPageNum.getAttribute("onclick"));
        String test = lastPageNum.getAttribute("onclick");
        //그냥 해당 버튼 누르지 말고 onclick의 text에서 값 뽑아내기
        //정규 표현식 사용
        //최대 페이지
        String extractedNumber = test.replaceAll("\\D+", "");//\\D : 숫자가 아닌 문자
        System.out.println(extractedNumber); // 출력: "206"
        int aa = Integer.valueOf(extractedNumber);
        System.out.println("숫자변환 : " + aa);


        return null;
    }
}
