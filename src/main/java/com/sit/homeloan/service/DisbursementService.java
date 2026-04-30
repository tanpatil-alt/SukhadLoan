package com.sit.homeloan.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.sit.homeloan.dto.DisbursementDTO;
import com.sit.homeloan.dto.SanctionedLoanDTO;
import com.sit.homeloan.model.Disbursement;
import com.sit.homeloan.model.LoanApplication;

public interface DisbursementService {
	String disburseLoan(Long loanAppId, Double amount);

	ResponseEntity<?> getByLoanAppId(Long loanAppId);

	Disbursement getDisbursementObjectByLoanAppId(Long loanAppId);

	List<SanctionedLoanDTO> getSanctionedApplications();

	SanctionedLoanDTO getSanctionedApplicationDetails(Long loanAppId);
	
	List<DisbursementDTO> getProcessedDisbursements();
}
