package org.spring.oneplusone.Utils.Error;

import lombok.extern.slf4j.Slf4j;

import org.spring.oneplusone.Utils.Enums.ErrorList;
import org.spring.oneplusone.Utils.Response.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//나중에 커스텀 할때 사용
 import org.spring.oneplusone.Utils.Enums.ErrorList;

//basePackages옵션을 사용하면 특정 클래스만 제한적으로 적용 가능
@Slf4j
@RestControllerAdvice
//Spring 예외를 미리 처리해둔 추상 클래스를 상속 받음
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({NullPointerException.class})
    protected ResponseEntity<ErrorResponse> handleBadRequest(){
        log.error(ErrorList.BADREQUEST.getHttpStatus().name());
        log.error(ErrorList.BADREQUEST.getCode());
        log.error(ErrorList.BADREQUEST.getErrmsg());
        ErrorResponse errorResponse = new ErrorResponse(ErrorList.BADREQUEST.isSuccess(),ErrorList.BADREQUEST.getCode());
        log.debug("ErrorResponse: {}, {}", errorResponse.getResult(), errorResponse.isSuccess());
        ResponseEntity<ErrorResponse> responseEntity = new ResponseEntity<>(errorResponse, ErrorList.BADREQUEST.getHttpStatus());
        log.debug("ResponseEntity: {}, {}", responseEntity.getHeaders().getContentLength(), responseEntity.getHeaders().getContentType());
        return responseEntity;
    }
    @ExceptionHandler({CustomException.class})
    protected ResponseEntity<ErrorResponse> handleServerException(CustomException ex) {
        log.error(ex.error.getHttpStatus().name());
        log.error(ex.error.getCode());
        log.error(ex.error.getErrmsg());
        ErrorResponse errorResponse = new ErrorResponse( ex.error.isSuccess(),ex.error.getCode());
        log.debug("ErrorResponse: {}, {}", errorResponse.getResult(), errorResponse.isSuccess());
        ResponseEntity<ErrorResponse> responseEntity = new ResponseEntity<>(errorResponse, ex.error.getHttpStatus());
        log.debug("ResponseEntity: {}, {}", responseEntity.getHeaders().getContentLength(), responseEntity.getHeaders().getContentType());
        return responseEntity;
    }

}
