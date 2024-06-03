package org.spring.oneplusone.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SearchDTO {
    private String productName;

    @Builder
    SearchDTO(String productName){
        this.productName = productName;
    }
}
