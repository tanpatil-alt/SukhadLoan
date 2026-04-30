package com.sit.homeloan.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.sit.homeloan.dto.CustomerProfileDTO;
import com.sit.homeloan.dto.LoanApplicationDTO;
import com.sit.homeloan.dto.LoanApplicationRequestDTO;
import com.sit.homeloan.dto.SanctionDetailsDTO;
import com.sit.homeloan.enums.DocumentType;
import com.sit.homeloan.model.Customer;
import com.sit.homeloan.model.Documents;
import com.sit.homeloan.model.LoanApplication;

import org.springframework.core.io.Resource;

public interface CustomerService {

	CustomerProfileDTO getCustomerProfileDTO(String email);

	String applyForLoan(LoanApplicationRequestDTO requestDTO);

	String deleteLoanApplication(Long applicationId, String email);

	String uploadDocument(MultipartFile file, String email, DocumentType documentType);

	List<LoanApplicationDTO> getMyLoanApplications(String email);

	List<Documents> getMyDocuments(String email);

	public ResponseEntity<Resource> downloadDocument(String fileName);
	
	SanctionDetailsDTO getSanctionDetailsByEmailAndLoanId(String email, Long loanAppId);

    
    ResponseEntity<Resource> downloadSanctionLetterPdfByEmailAndLoanId(String email, Long loanAppId);

}
