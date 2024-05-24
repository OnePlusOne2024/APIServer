package org.spring.oneplusone.Utils.Response;

import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.Utils.Enums.ErrorList;

@Getter
@Setter
public class ErrorResponse {
    private boolean success;
    private ErrorList result;

    public ErrorResponse(ErrorList result, boolean success){
        this.success = success;
        this.result = result;
    }
}
