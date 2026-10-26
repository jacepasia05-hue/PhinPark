package com.phinpark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.phinpark.entity.TicketEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Integer> {

    List<TicketEntity> findByExitTimeIsNullOrderByEntryTimeDesc();

    List<TicketEntity> findByExitTimeIsNotNullOrderByExitTimeDesc();

    List<TicketEntity> findByOwnerIdAndExitTimeIsNullOrderByEntryTimeDesc(Integer ownerId);

    List<TicketEntity> findByOwnerIdAndExitTimeIsNotNullOrderByExitTimeDesc(Integer ownerId);

    boolean existsByPlateNumberIgnoreCaseAndExitTimeIsNull(String plateNumber);

    List<TicketEntity> findByPlateNumberIgnoreCaseAndExitTimeIsNull(String plateNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ticket from TicketEntity ticket where ticket.id = :id and ticket.exitTime is null")
    Optional<TicketEntity> findActiveByIdForUpdate(@Param("id") Integer id);
}
