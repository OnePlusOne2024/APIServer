package org.spring.oneplusone.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import org.spring.oneplusone.Utils.Enums.ConvName;

import java.io.Serializable;

@Embeddable
@Getter
public class ProductId implements Serializable {
    @Column(length = 60, nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(length = 24, nullable = false)
    private ConvName convname;

    public ProductId(){};
    public ProductId(String name, ConvName convname){
        this.name = name;
        this.convname = convname;
    }
}
