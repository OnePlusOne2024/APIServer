package org.spring.oneplusone.DTO;


import lombok.*;

@Getter //값 읽어오기
public class CrawlingResultDTO {
    private int resultCount;
    @Builder
    public CrawlingResultDTO(int resultCount){
        this.resultCount = resultCount;
    }
}
