package org.spring.oneplusone.Utils.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor//모든 변수 필요
public enum Category {
    DRINK("음료"),
    FOOD("식품"),
    SNACK("과자"),
    ICECREAM("아이스크림"),
    LIVING_PRODUCT("생활 용품"),
    NOT_FOUND("미정");
    private final String categoryName;
}
