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
    private double longitude;//longitude
    private double latitude;//latitude
    @Builder
    ConvDTO(String convName, String convAddr, double longitude, double latitude , ConvName convBrandName){
        this.convAddr = convAddr;
        this.longitude = longitude;
        this.latitude = latitude;
        this.convName = convName;
        this.convBrandName = convBrandName;
    }
}
