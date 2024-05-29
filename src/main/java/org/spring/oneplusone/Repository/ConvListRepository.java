package org.spring.oneplusone.Repository;

import org.spring.oneplusone.Entity.ConvEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConvListRepository extends JpaRepository<ConvEntity, String> {
    @Query(value = "SELECT * FROM conv_list WHERE ST_Distance_Sphere(point(longitude,latitude), point(?1,?2)) <= ?3", nativeQuery = true)
    List<ConvEntity> findNearConv(double longitude, double latitude, double distanceInMeters);
}
