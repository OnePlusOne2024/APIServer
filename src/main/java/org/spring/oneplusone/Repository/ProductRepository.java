package org.spring.oneplusone.Repository;

import org.spring.oneplusone.Entity.ProductEntity;
import org.spring.oneplusone.Entity.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.Repository;

import java.util.List;


public interface ProductRepository extends JpaRepository<ProductEntity, ProductId> {
    List<ProductEntity> findByPid(ProductId pid);
}
