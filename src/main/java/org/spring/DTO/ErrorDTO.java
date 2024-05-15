package org.spring.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor//모든 변수에 대해 생성자 생성
public class ErrorDTO {
    //에러코드
//    private Integer code;
    //에러 메시지
    private String msg;



}
