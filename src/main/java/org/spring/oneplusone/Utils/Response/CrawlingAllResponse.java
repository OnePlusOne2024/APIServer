package org.spring.oneplusone.Utils.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.DTO.CrawlingResultDTO;

@Getter
@Setter
public class CrawlingAllResponse implements BasicResponse<CrawlingResultDTO> {
    private boolean success;
    private CrawlingResultDTO result;
    @Builder
    public CrawlingAllResponse(CrawlingResultDTO result, boolean success) {
        this.success = success;
        this.result = result;
    }
}
