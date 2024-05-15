package org.spring.Utils.Response;

import lombok.Getter;
import lombok.Setter;
import org.spring.DTO.CrawlingResultDTO;
import org.spring.DTO.ProductDTO;

import java.util.List;

@Getter
@Setter
public class ProductCrawlingAllResponse {
    private Boolean success;
    private CrawlingResultDTO result;

    public ProductCrawlingAllResponse(CrawlingResultDTO crawlingResultDTO, Boolean success) {
        this.success = success;
        this.result = crawlingResultDTO;
    }
}
