package com.sit.homeloan.service;

import com.sit.homeloan.dto.LoanApplicationDTO;


import com.sit.homeloan.dto.LoanApplicationDetailsDTO;
import com.sit.homeloan.dto.LoanApplicationforsanctionDTO;
import com.sit.homeloan.dto.LoanWithDocumentsDTO;
import com.sit.homeloan.model.Documents;
import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.model.SanctionLetter;

import org.springframework.http.ResponseEntity;

import java.util.List;

import org.springframework.core.io.Resource;

public interface CreditManagerService {

	List<LoanApplicationDetailsDTO> getApplicationsWithDocumentsSubmitted();

	LoanWithDocumentsDTO getLoanWithDocuments(Long loanAppId);

	void updateVerificationStatus(Long documentId, String status);
	
	List<LoanApplicationDTO> getApplicationsReadyForEvaluation();

	void evaluateLoanApplication(Long loanApplicationId);
	
	 List<LoanApplicationforsanctionDTO> getEvaluatedApplications();

	SanctionLetter generateSanctionLetter(Long loanApplicationId, SanctionLetter input);

	SanctionLetter getSanctionLetter(Long loanApplicationId);
	
	 ResponseEntity<Resource> downloadDocument(String fileName);
	

}
