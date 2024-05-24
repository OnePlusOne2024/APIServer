package org.spring.oneplusone.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor//모든 변수에 대해 생성자 생성
public class ErrorDTO {
    private String msg;
}
