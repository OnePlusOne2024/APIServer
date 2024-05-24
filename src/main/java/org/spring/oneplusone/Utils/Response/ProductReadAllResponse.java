package org.spring.oneplusone.Utils.Response;

import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.DTO.ProductDTO;

import java.util.List;

@Getter
@Setter
public class ProductReadAllResponse implements BasicResponse<List<ProductDTO>> {
    private boolean success;
    private List<ProductDTO> result;

    public ProductReadAllResponse(List<ProductDTO> result, boolean success) {
        this.success = success;
        this.result = result;
    }
}
