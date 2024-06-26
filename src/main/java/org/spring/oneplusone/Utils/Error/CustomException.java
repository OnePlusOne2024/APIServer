package org.spring.oneplusone.Utils.Error;

import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.Utils.Enums.ErrorList;

@Getter
@Setter
public class CustomException extends RuntimeException{//프로그램 실행 중 에러가 발생해도 멈추기 않기 위해 runtimeException사용
    ErrorList error;
    public CustomException(ErrorList errorList) {
        this.error = errorList;
    }
}
