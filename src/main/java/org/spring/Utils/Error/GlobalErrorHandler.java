package org.spring.Utils.Error;

import org.spring.DTO.ErrorDTO;
import org.spring.Utils.Enums.ErrorList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
//나중에 커스텀 할때 사용
// import org.spring.Utils.Enums.ErrorList;

//basePackages옵션을 사용하면 특정 클래스만 제한적으로 적용 가능
@RestControllerAdvice
//Spring 예외를 미리 처리해둔 추상 클래스를 상속 받음
public class GlobalErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({Exception.class})
    protected ResponseEntity handleServerException(Exception ex){
        System.out.println(ex);
        return new ResponseEntity(
                new ErrorDTO(ErrorList.GSEVENT.getErrmsg())//새로운 객체를 통해서 DTO에 Errormsg 넣기(생성자를 통해서)
                    , HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
//    @ExceptionHandler({GSEventException.class})
//    protected ResponseEntity handleGSEventProductCrawlingException(Exception ex){
//        return new ResponseEntity(
//                new ErrorDTO((ErrorList.GSEVENT.getErrmsg())),HttpStatus.INTERNAL_SERVER_ERROR
//        );
//    }
}
