package com.sit.homeloan.repository;

import com.sit.homeloan.enums.ApplicationStatus;
import com.sit.homeloan.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
	
    List<LoanApplication> findByCustomer_User_Email(String email);
    
    List<LoanApplication> findByApplicationStatus(ApplicationStatus status);
    

}
