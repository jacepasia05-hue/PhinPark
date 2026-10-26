package com.phinpark.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.phinpark.entity.ParkingSlotEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlotEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select slot from ParkingSlotEntity slot where slot.slotNumber = :slotNumber")
    Optional<ParkingSlotEntity> findBySlotNumberForUpdate(@Param("slotNumber") Integer slotNumber);
}
