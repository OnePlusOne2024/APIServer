package org.spring.oneplusone.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="search", indexes = {
        @Index(name = "idx_product", columnList = "product_name")
})
public class SearchEntity {

    @Id
    private String id;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private int searchCount;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 생성자, getter, setter 생략
    @Builder
    SearchEntity(String id, String productName, int searchCount){
        this.productName = productName;
        this.id = id;
        this.searchCount = searchCount;
    }
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    public void incrementSearchCount() {
        this.searchCount++;
    }
    public void decrementSearchCount() {
        this.searchCount--;
    }
}