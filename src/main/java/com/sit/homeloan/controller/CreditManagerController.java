package com.sit.homeloan.controller;

import com.sit.homeloan.dto.LoanApplicationDTO;
import com.sit.homeloan.dto.LoanApplicationDetailsDTO;
import com.sit.homeloan.dto.LoanApplicationforsanctionDTO;
import com.sit.homeloan.dto.LoanWithDocumentsDTO;
import com.sit.homeloan.model.Documents;
import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.model.SanctionLetter;
import com.sit.homeloan.service.CreditManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/credit-manager")
//@CrossOrigin("*")
public class CreditManagerController {

	@Autowired
	private CreditManagerService creditManagerService;

	@GetMapping("/loan-applications/submitted")
	public List<LoanApplicationDetailsDTO> getSubmittedApplications() {
		return creditManagerService.getApplicationsWithDocumentsSubmitted();
	}

	@GetMapping("/loan-with-docs/{loanAppId}")
	public LoanWithDocumentsDTO getLoanDetailsWithDocuments(@PathVariable Long loanAppId) {
		return creditManagerService.getLoanWithDocuments(loanAppId);
	}

	@PutMapping("/documents/verify/{id}")
	public String updateDocumentStatus(@PathVariable Long id, @RequestParam String status) {
		creditManagerService.updateVerificationStatus(id, status);
		return "Document status updated to: " + status.toUpperCase();
	}
	
	
	@GetMapping("/download-document")
	public ResponseEntity<Resource> downloadDocument(@RequestParam String fileName) {
	    return creditManagerService.downloadDocument(fileName);
	}


	@GetMapping("/applications-to-evaluate")
	public ResponseEntity<List<LoanApplicationDTO>> getApplicationsToEvaluate() {
		List<LoanApplicationDTO> applications = creditManagerService.getApplicationsReadyForEvaluation();
		return ResponseEntity.ok(applications);
	}

	@PutMapping("/evaluate/{loanAppId}")
	public String evaluateLoan(@PathVariable Long loanAppId) {
		creditManagerService.evaluateLoanApplication(loanAppId);
		return "Loan application evaluated successfully!";
	}

	@GetMapping("/evaluated-applications")
	public List<LoanApplicationforsanctionDTO> getEvaluatedApplications() {
		return creditManagerService.getEvaluatedApplications();
	}

	@PostMapping("/sanction/{loanAppId}")
	public SanctionLetter generateSanction(@PathVariable Long loanAppId, @RequestBody SanctionLetter request) {
		return creditManagerService.generateSanctionLetter(loanAppId, request);
	}

	@GetMapping("/sanction/{loanAppId}")
	public SanctionLetter getSanction(@PathVariable Long loanAppId) {
		return creditManagerService.getSanctionLetter(loanAppId);
	}
}
