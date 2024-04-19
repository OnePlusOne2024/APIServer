package org.spring.oneplusone.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import org.spring.oneplusone.Global.Enums.ConvName;

import java.io.Serializable;

@Embeddable
@Getter
public class ProductId implements Serializable {
    @Column(length = 60)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private ConvName convname;
}
