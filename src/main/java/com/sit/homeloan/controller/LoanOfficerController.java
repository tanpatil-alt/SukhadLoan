package com.sit.homeloan.controller;

import com.sit.homeloan.dto.LoanApplicationDTO;

import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.service.LoanOfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-officer")
//@CrossOrigin("*")
public class LoanOfficerController {

	@Autowired
	private LoanOfficerService loanOfficerService;

	@GetMapping("/pending-applications")
	public List<LoanApplicationDTO> getPendingApplications() {
		return loanOfficerService.getAllPendingApplications();
	}

	
	@PostMapping("/review-cibil")
	public String reviewCibilDecision(@RequestParam Long applicationId, @RequestParam String officerEmail,
			@RequestParam boolean reject, @RequestParam(required = false) String reasonIfRejected) {
		return loanOfficerService.reviewCIBILDecision(applicationId, officerEmail, reject, reasonIfRejected);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
