package org.spring.oneplusone.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "crawling")
public class CrawlingTimeEntity {
    @Id
    private Long id; //1로 고정

    @Column(nullable = false)
    private LocalDateTime latestCrawlingTime;

    @Builder
    public CrawlingTimeEntity(Long id, LocalDateTime latestCrawlingTime){
        this.latestCrawlingTime = latestCrawlingTime;
        this.id = id;
    }
}
