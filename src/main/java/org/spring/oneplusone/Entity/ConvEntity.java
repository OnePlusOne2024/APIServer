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
        @Index(name = "idx_coordinates_xy", columnList = "x, y")
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
    private double x; //longitude
    @Column(nullable = false)
    private double y; //latitude

    @Builder
    ConvEntity(String convName, String convAddr, double x, double y , String id, ConvName convBrandName){
        this.convAddr = convAddr;
        this.x = x;
        this.y = y;
        this.convName = convName;
        this.id = id;
        this. convBrandName = convBrandName;
    }

}
