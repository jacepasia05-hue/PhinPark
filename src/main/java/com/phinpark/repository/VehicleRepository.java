package com.phinpark.repository;

import com.phinpark.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Integer> {
    List<VehicleEntity> findByOwnerId(Integer ownerId);

    Optional<VehicleEntity> findByPlateNumberIgnoreCase(String plateNumber);
}
