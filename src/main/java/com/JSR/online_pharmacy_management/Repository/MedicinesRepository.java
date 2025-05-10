package com.JSR.online_pharmacy_management.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JSR.online_pharmacy_management.Entity.Medicines;

@Repository
public interface MedicinesRepository extends  JpaRepository<Medicines, Long> {

}
