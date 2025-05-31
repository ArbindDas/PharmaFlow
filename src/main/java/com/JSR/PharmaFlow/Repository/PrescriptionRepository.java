package com.JSR.PharmaFlow.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

}
