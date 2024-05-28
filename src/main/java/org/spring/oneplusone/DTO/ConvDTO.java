package org.spring.oneplusone.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.spring.oneplusone.Utils.Enums.ConvName;


@Getter
public class ConvDTO {
    private ConvName convBrandName;
    private String convName;
    private String convAddr;
    private double x;
    private double y;
    @Builder
    ConvDTO(String convName, String convAddr, double x, double y , ConvName convBrandName){
        this.convAddr = convAddr;
        this.x = x;
        this.y = y;
        this.convName = convName;
        this.convBrandName = convBrandName;
    }
}
