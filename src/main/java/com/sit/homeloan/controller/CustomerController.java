package com.sit.homeloan.controller;

import com.sit.homeloan.dto.CustomerProfileDTO;



import com.sit.homeloan.dto.LoanApplicationDTO;
import com.sit.homeloan.dto.LoanApplicationRequestDTO;
import com.sit.homeloan.dto.SanctionDetailsDTO;
import com.sit.homeloan.enums.DocumentType;
import com.sit.homeloan.model.Customer;
import com.sit.homeloan.model.Documents;
import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.service.CustomerService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/customer")
//@CrossOrigin(origins = "http://localhost:4200")

public class CustomerController {
	@Autowired
	private CustomerService customerService;

	@GetMapping("/profile")
	public CustomerProfileDTO getProfile(@RequestParam String email) {
		return customerService.getCustomerProfileDTO(email);
	}

	@PostMapping("/apply-loan")
	public String applyLoan(@RequestBody LoanApplicationRequestDTO requestDTO) {
		return customerService.applyForLoan(requestDTO);
	}

	@GetMapping("/my-applications")
	public List<LoanApplicationDTO> getLoanApplications(@RequestParam String email) {
		return customerService.getMyLoanApplications(email);
	}

	@PostMapping("/upload-document")
	public String uploadDocument(@RequestParam("file") MultipartFile file, 
	                             @RequestParam("email") String email,
	                             @RequestParam("type") DocumentType documentType) {
	    return customerService.uploadDocument(file, email, documentType);
	}

	@GetMapping("/download-document")
	public ResponseEntity<Resource> downloadDocument(@RequestParam String fileName) {
	    return customerService.downloadDocument(fileName);
	}


	@GetMapping("/my-documents")

	public List<Documents> getDocuments(@RequestParam String email) {
		return customerService.getMyDocuments(email);
	}

	@DeleteMapping("/delete-loan")
	public String deleteLoanApplication(@RequestParam Long applicationId, @RequestParam String email) {
		return customerService.deleteLoanApplication(applicationId, email);
	}
	
	
	
	 @GetMapping("/sanction-details/{loanAppId}")
	    public ResponseEntity<SanctionDetailsDTO> getSanctionDetails(
	            @RequestParam String email,
	            @PathVariable Long loanAppId) {

	        SanctionDetailsDTO dto = customerService.getSanctionDetailsByEmailAndLoanId(email, loanAppId);
	        if (dto == null) {
	            return ResponseEntity.notFound().build();
	        }
	        return ResponseEntity.ok(dto);
	    }

	    @GetMapping("/sanction-letter/download/{loanAppId}")
	    public ResponseEntity<Resource> downloadSanctionLetterPdf(
	            @RequestParam String email,
	            @PathVariable Long loanAppId) {

	        return customerService.downloadSanctionLetterPdfByEmailAndLoanId(email, loanAppId);
	    }

}
