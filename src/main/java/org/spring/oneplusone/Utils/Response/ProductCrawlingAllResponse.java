package org.spring.oneplusone.Utils.Response;

import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.DTO.CrawlingResultDTO;

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
