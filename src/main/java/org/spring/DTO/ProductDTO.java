package org.spring.DTO;


import lombok.Builder;
import lombok.Getter;
import org.spring.Utils.Enums.ConvName;
//import lombok.NoArgsConstructor;
//import lombok.AccessLevel;


@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)//아래에서 생성자를 만들었음(@Builder 사용)
public class ProductDTO {
    private String name;
    private ConvName convname;
    private int price;
    private Boolean pb;
    private String event;
    private String category;
    private String image;

    @Builder
    ProductDTO(String name, ConvName convname, Integer price, Boolean pb, String event, String category, String image){
        this.name = name;
        this.convname = convname;
        this.price = price;
        this.pb = pb;
        this.event = event;
        this.category = category;
        this.image = image;
    }
}
