package org.spring.oneplusone.Repository;

import org.spring.oneplusone.Entity.SearchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SearchRepository extends JpaRepository<SearchEntity, Long> {
    SearchEntity findByProductName(String productName);
}
