package com.sit.homeloan.repository;

import com.sit.homeloan.enums.ApplicationStatus;
import com.sit.homeloan.model.Disbursement;
import com.sit.homeloan.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {
	Optional<Disbursement> findByLoanApplication(LoanApplication loanApplication);

	

}
