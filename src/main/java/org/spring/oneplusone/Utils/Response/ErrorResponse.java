package org.spring.oneplusone.Utils.Response;

import lombok.*;
import org.spring.oneplusone.Utils.Enums.ErrorList;

@Getter
@Setter
public class ErrorResponse implements BasicResponse<String>{
    private boolean success;
    private String result;

    @Builder
    public ErrorResponse(boolean success, String result){
        this.success = success;
        this.result = result;
    }
}
