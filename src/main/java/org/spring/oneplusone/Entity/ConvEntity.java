package org.spring.oneplusone.Entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.spring.oneplusone.Utils.Enums.ConvName;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "conv_list", indexes = {
        @Index(name = "idx_coordinates_longitude_latitude", columnList = "longitude, latitude")
})
public class ConvEntity {
    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(length = 24, nullable = false)
    private ConvName convBrandName;
    @Column(nullable = false)
    private String convName;
    @Column(nullable = false)
    private String convAddr;
    @Column(nullable = false)
    private double longitude; //longitude
    @Column(nullable = false)
    private double latitude; //latitude

    @Builder
    ConvEntity(String convName, String convAddr, double longitude, double latitude , String id, ConvName convBrandName){
        this.convAddr = convAddr;
        this.longitude = longitude;
        this.latitude = latitude;
        this.convName = convName;
        this.id = id;
        this. convBrandName = convBrandName;
    }

}
