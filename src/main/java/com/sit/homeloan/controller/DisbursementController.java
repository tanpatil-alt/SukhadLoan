package com.sit.homeloan.controller;

import com.sit.homeloan.dto.DisbursementDTO;
import com.sit.homeloan.dto.SanctionedLoanDTO;
import com.sit.homeloan.model.Disbursement;
import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.service.DisbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/disbursements")
public class DisbursementController {

	@Autowired
	private DisbursementService disbursementService;

	@GetMapping("/sanctioned")
	public ResponseEntity<List<SanctionedLoanDTO>> getAllSanctionedLoans() {
		List<SanctionedLoanDTO> loans = disbursementService.getSanctionedApplications();
		return ResponseEntity.ok(loans);
	}

	@GetMapping("/sanctioned/{loanAppId}")
	public ResponseEntity<SanctionedLoanDTO> getSanctionedLoanDetails(@PathVariable Long loanAppId) {
		SanctionedLoanDTO loanDetails = disbursementService.getSanctionedApplicationDetails(loanAppId);
		return loanDetails != null ? ResponseEntity.ok(loanDetails) : ResponseEntity.notFound().build();
	}

	@PostMapping("/process/{loanAppId}")
	public ResponseEntity<String> processDisbursement(@PathVariable Long loanAppId,
			@RequestBody Map<String, Double> request) {
		Double amount = request.get("amount");
		String result = disbursementService.disburseLoan(loanAppId, amount);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/details/{loanAppId}")
	public ResponseEntity<?> getDisbursementDetails(@PathVariable Long loanAppId) {
		return disbursementService.getByLoanAppId(loanAppId);
	}

	@GetMapping("/object/{loanAppId}")
	public ResponseEntity<Disbursement> getDisbursementObject(@PathVariable Long loanAppId) {
		Disbursement disbursement = disbursementService.getDisbursementObjectByLoanAppId(loanAppId);
		return disbursement != null ? ResponseEntity.ok(disbursement) : ResponseEntity.notFound().build();
	}

	@GetMapping("/processed")
	public ResponseEntity<List<DisbursementDTO>> getProcessedDisbursements() {
	    List<DisbursementDTO> processed = disbursementService.getProcessedDisbursements();
	    return ResponseEntity.ok(processed);
	}


}