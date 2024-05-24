package org.spring.oneplusone.Utils.Response;

import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.DTO.CrawlingResultDTO;

@Getter
@Setter
public class ProductCrawlingAllResponse implements BasicResponse<CrawlingResultDTO> {
    private boolean success;
    private CrawlingResultDTO result;

    public ProductCrawlingAllResponse(CrawlingResultDTO result, boolean success) {
        this.success = success;
        this.result = result;
    }
}
