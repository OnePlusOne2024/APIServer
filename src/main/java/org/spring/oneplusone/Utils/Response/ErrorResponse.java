package org.spring.oneplusone.Utils.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.spring.oneplusone.Utils.Enums.ErrorList;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String result;

}
