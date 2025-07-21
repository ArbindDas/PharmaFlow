package com.JSR.PharmaFlow.Repository;

import com.JSR.PharmaFlow.Enums.MedicineStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.Medicines;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicinesRepository extends  JpaRepository<Medicines, Long> {

    @Query ("SELECT m FROM Medicines m WHERE m.id = :id")
    Optional <Medicines> findByIdWithoutOrderItems( @Param ("id") Long id);

    List<Medicines> findByMedicineStatus(MedicineStatus status);

    List<Medicines> findByMedicineStatusIn(List<String> statuses);

    // This should be provided by JpaRepository by default
    Optional<Medicines> findById(Long id);

}
