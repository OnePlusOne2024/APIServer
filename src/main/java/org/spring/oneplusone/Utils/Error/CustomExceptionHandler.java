package org.spring.oneplusone.Utils.Error;

import lombok.extern.slf4j.Slf4j;

import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//나중에 커스텀 할때 사용
 import org.spring.oneplusone.Utils.Enums.ErrorList;

//basePackages옵션을 사용하면 특정 클래스만 제한적으로 적용 가능
@Slf4j
@RestControllerAdvice
//Spring 예외를 미리 처리해둔 추상 클래스를 상속 받음
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({CustomException.class})
    protected ResponseEntity handleServerException(CustomException ex){
        log.error(ex.errorList.getCode());
        log.error(ex.errorList.getErrmsg());
        return new ResponseEntity(ex.errorList.getCode(), ex.errorList.getHttpStatus());
    }


}
