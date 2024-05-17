package org.spring.oneplusone.Utils.Error;

import org.spring.oneplusone.Utils.Enums.ErrorList;

public class CustomException extends RuntimeException{//프로그램 실행 중 에러가 발생해도 멈추기 않기 위해 runtimeException사용
    ErrorList errorList;
}
