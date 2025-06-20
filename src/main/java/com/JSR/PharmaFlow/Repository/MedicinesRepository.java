package com.JSR.PharmaFlow.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.Medicines;

import java.util.List;

@Repository
public interface MedicinesRepository extends  JpaRepository<Medicines, Long> {


}
