package org.spring.oneplusone.DTO;


import lombok.*;

@AllArgsConstructor //객체 생성
@Getter //값 읽어오기
@Setter //생성 후에 값 변경
//@NoArgsConstructor(access = AccessLevel.PROTECTED)//아래에서 생성자를 만들었음(@Builder 사용)
public class CrawlingResultDTO {
    private Integer resultCount;
}
