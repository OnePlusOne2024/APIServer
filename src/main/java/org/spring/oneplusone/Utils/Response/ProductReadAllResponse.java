package org.spring.oneplusone.Utils.Response;

import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.DTO.ProductDTO;

import java.util.List;

@Getter
@Setter
public class ProductReadAllResponse {
    private Boolean success;
    private List<ProductDTO> result;

    public ProductReadAllResponse(List<ProductDTO> productDTOList, Boolean success) {
        this.success = success;
        this.result = productDTOList;
    }
}
