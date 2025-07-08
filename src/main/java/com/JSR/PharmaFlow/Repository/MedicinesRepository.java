package com.JSR.PharmaFlow.Repository;

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

    List<Medicines> findByStatus(String status);

    List< Medicines> findByStatusIn( List< String> approved );
}
