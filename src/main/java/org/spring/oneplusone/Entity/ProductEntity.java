package org.spring.oneplusone.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.spring.oneplusone.Global.Enums.ConvName;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "product")
public class ProductEntity {
    @Column
    private int price;
    @Column
    private Boolean pb;
    @Column(length = 6)
    private String event;
    @Column(length = 10)
    private String category;
    @Column(length = 255)
    private String image;

    @EmbeddedId
    private ProductId pid;

    @Builder
    public ProductEntity(ProductId pid,int price, Boolean pb, String event, String category, String image){
        this.pid = pid;
        this.price = price;
        this.pb = pb;
        this.event = event;
        this.category = category;
        this.image = image;
    }
    //pid를 처리해주는 custom method
}

