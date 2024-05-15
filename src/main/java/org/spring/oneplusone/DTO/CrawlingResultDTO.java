package org.spring.oneplusone.DTO;


import lombok.*;

@AllArgsConstructor //객체 생성
@Getter //값 읽어오기
@Setter //생성 후에 값 변경
//@NoArgsConstructor(access = AccessLevel.PROTECTED)//아래에서 생성자를 만들었음(@Builder 사용)
public class CrawlingResultDTO {
    private Boolean success;
    private Integer resultCount;
// 생성자에 값이 입력되지 않아도 되는 걸로 만듬
//    @Builder
//    CrawlingResultDTO(Boolean success, Integer resultCount){
//        //생성자를 통해 값을 받음
//        this.success = success;
//        this.resultCount = resultCount;
//    }

}
