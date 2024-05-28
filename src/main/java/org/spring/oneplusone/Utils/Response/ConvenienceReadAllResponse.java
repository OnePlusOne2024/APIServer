package org.spring.oneplusone.Utils.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.DTO.ConvDTO;

import java.util.List;
@Getter
@Setter
public class ConvenienceReadAllResponse implements BasicResponse<List<ConvDTO>>{
    private boolean success;
    private List<ConvDTO> result;
    @Builder
    public ConvenienceReadAllResponse(boolean success, List<ConvDTO> result){
        this.success = success;
        this.result = result;
    }
}
