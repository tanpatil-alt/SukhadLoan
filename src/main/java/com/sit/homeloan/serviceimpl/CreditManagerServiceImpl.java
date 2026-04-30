package com.sit.homeloan.serviceimpl;

import com.sit.homeloan.dto.DocumentDto;

import com.sit.homeloan.dto.LoanApplicationDTO;
import com.sit.homeloan.dto.LoanApplicationDetailsDTO;
import com.sit.homeloan.dto.LoanApplicationforsanctionDTO;
import com.sit.homeloan.dto.LoanWithDocumentsDTO;
import com.sit.homeloan.enums.ApplicationStatus;
import com.sit.homeloan.enums.VerificationStatus;
import com.sit.homeloan.model.CreditEvaluation;
import com.sit.homeloan.model.Customer;
import com.sit.homeloan.model.Documents;
import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.model.SanctionLetter;
import com.sit.homeloan.repository.CreditEvaluationRepository;
import com.sit.homeloan.repository.CreditManagerRepository;
import com.sit.homeloan.repository.DocumentRepository;
import com.sit.homeloan.repository.LoanApplicationRepository;
import com.sit.homeloan.repository.SanctionLetterRepository;
import com.sit.homeloan.service.CreditManagerService;
import com.sit.homeloan.service.LoanStageHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CreditManagerServiceImpl implements CreditManagerService {

	@Autowired
	private CreditManagerRepository creditManagerRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private LoanStageHistoryService loanStageHistoryService;

	@Autowired
	private CreditEvaluationRepository creditEvaluationRepository;

	@Autowired
	private SanctionLetterRepository sanctionLetterRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Override
	public List<LoanApplicationDetailsDTO> getApplicationsWithDocumentsSubmitted() {
		List<LoanApplication> applications = loanApplicationRepository
				.findByApplicationStatus(ApplicationStatus.DOCUMENT_SUBMITTED);

		List<LoanApplicationDetailsDTO> dtos = new ArrayList<>();

		for (LoanApplication application : applications) {
			Customer customer = application.getCustomer();
			LoanApplicationDetailsDTO dto = new LoanApplicationDetailsDTO();
			dto.setApplicationId(application.getId());
			dto.setFullName(customer.getUser().getFullName());
			dto.setEmail(customer.getUser().getEmail());
			dto.setPanNumber(customer.getPanNumber());
			dto.setAadhaarNumber(customer.getAadhaarNumber());
			dto.setAddress(customer.getAddress());
			dto.setEmploymentType(customer.getEmploymentType());
			dto.setEmployerName(customer.getEmployerName());
			dto.setMonthlyIncome(customer.getMonthlyIncome());
			dto.setBankAccountNumber(customer.getBankAccountNumber());
			dto.setAccountHolderName(customer.getAccountHolderName());
			dto.setIfscCode(customer.getIfscCode());
			dto.setLoanPurpose(application.getLoanPurpose());
			dto.setLoanAmount(application.getLoanAmount());
			dto.setLoanTenureInMonths(application.getLoanTenureInMonths());
			dto.setApplicationDate(application.getApplicationDate().toString());

			dtos.add(dto);
		}

		return dtos;
	}

	@Override
	public LoanWithDocumentsDTO getLoanWithDocuments(Long loanAppId) {
		LoanWithDocumentsDTO dto = new LoanWithDocumentsDTO();

		LoanApplication loan = loanApplicationRepository.findById(loanAppId).orElse(null);
		if (loan == null)
			return null;

		Customer customer = loan.getCustomer();
		List<Documents> documents = customer != null ? documentRepository.findByCustomerId(customer.getId())
				: new ArrayList<>();

		List<DocumentDto> docDtos = documents.stream().map(doc -> {
			DocumentDto d = new DocumentDto();
			d.setId(doc.getId());
			d.setName(doc.getFileName());
			d.setFileType(doc.getFileType());
			d.setDocumentType(doc.getDocumentType().name());
			d.setVerificationStatus(doc.getVerificationStatus().name());

			if (doc.getFileData() != null) {
				d.setFileType(doc.getFileType());
				d.setData(new String(Base64.getEncoder().encode(doc.getFileData())));
			}

			return d;
		}).collect(Collectors.toList());

		dto.setLoan(loan);
		dto.setDocuments(docDtos);

		return dto;
	}
	
	
	

	@Override
	public ResponseEntity<Resource> downloadDocument(String fileName) {
	    List<Documents> documents = documentRepository.findAll();

	    for (Documents doc : documents) {
	        if (doc.getFileName().equals(fileName)) {
	            ByteArrayResource resource = new ByteArrayResource(doc.getFileData());

	            return ResponseEntity.ok()
	                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
	                    .header(HttpHeaders.CONTENT_TYPE, doc.getFileType())
	                    .body(resource);
	        }
	    }

	    return ResponseEntity.notFound().build();
	}



	@Override
	public void updateVerificationStatus(Long documentId, String status) {
		try {

			Documents document = creditManagerRepository.findById(documentId).orElse(null);

			if (document == null) {
				throw new RuntimeException("Document not found with ID: " + documentId);
			}

			VerificationStatus verificationStatus;
			try {
				verificationStatus = VerificationStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Invalid verification status: " + status);
			}

			document.setVerificationStatus(verificationStatus);
			creditManagerRepository.save(document);

			Customer customer = document.getCustomer();
			if (customer == null) {
				throw new RuntimeException("No customer associated with document");
			}

			List<LoanApplication> loanApplications = customer.getLoanApplications();
			if (loanApplications == null || loanApplications.isEmpty()) {
				throw new RuntimeException("No loan applications found for customer");
			}

			LoanApplication app = loanApplications.get(0);

			loanStageHistoryService.logStage(app.getId(), "Credit Manager", "CREDIT_MANAGER",
					app.getApplicationStatus().name(),
					"Updated document verification: " + document.getDocumentType() + " to " + status);

			List<Documents> allDocuments = documentRepository.findByCustomerId(customer.getId());
			if (allDocuments == null || allDocuments.isEmpty()) {
				throw new RuntimeException("No documents found for customer");
			}

			boolean allVerified = true;
			for (Documents doc : allDocuments) {
				if (!VerificationStatus.VERIFIED.equals(doc.getVerificationStatus())) {
					allVerified = false;
					break;
				}
			}

			if (allVerified) {
				app.setApplicationStatus(ApplicationStatus.ALL_DOCUMENT_VERIFIED);
				loanApplicationRepository.save(app);

				loanStageHistoryService.logStage(app.getId(), "Credit Manager", "CREDIT_MANAGER",
						ApplicationStatus.ALL_DOCUMENT_VERIFIED.name(), "All documents verified");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error updating verification status: " + e.getMessage(), e);
		}
	}

	@Override
	public List<LoanApplicationDTO> getApplicationsReadyForEvaluation() {
		List<LoanApplication> applications = loanApplicationRepository
				.findByApplicationStatus(ApplicationStatus.ALL_DOCUMENT_VERIFIED);

		List<LoanApplicationDTO> dtos = new ArrayList<>();

		for (LoanApplication application : applications) {
			LoanApplicationDTO dto = new LoanApplicationDTO();

			dto.setId(application.getId());
			dto.setLoanAmount(application.getLoanAmount());
			dto.setLoanTenureInMonths(application.getLoanTenureInMonths());
			dto.setCibilScore(application.getCibilScore());
			dto.setApplicationDate(application.getApplicationDate());

			if (application.getLoanPurpose() != null) {
				dto.setLoanPurpose(application.getLoanPurpose().toString());
			}
			if (application.getApplicationStatus() != null) {
				dto.setApplicationStatus(application.getApplicationStatus().toString());
			}

			if (application.getCustomer() != null && application.getCustomer().getUser() != null) {
				dto.setApplicantName(application.getCustomer().getUser().getFullName());
			} else {
				dto.setApplicantName("Unknown");
			}

			dtos.add(dto);
		}

		return dtos;
	}

	@Override
	public void evaluateLoanApplication(Long loanAppId) {
		Optional<LoanApplication> loanOpt = loanApplicationRepository.findById(loanAppId);

		if (!loanOpt.isPresent()) {
			throw new RuntimeException("Loan Application not found with ID: " + loanAppId);
		}

		LoanApplication loanApp = loanOpt.get();

		Long customerId = loanApp.getCustomer().getId();

		List<Documents> documentList = creditManagerRepository.getByCustomer_Id(customerId);

		boolean allVerified = true;

		for (int i = 0; i < documentList.size(); i++) {
			Documents doc = documentList.get(i);

			if (!doc.getVerificationStatus().equals(VerificationStatus.VERIFIED)) {
				allVerified = false;
				break;
			}
		}

		if (!allVerified) {
			throw new RuntimeException("All documents must be VERIFIED before evaluation");
		}

		CreditEvaluation evaluation = new CreditEvaluation();
		evaluation.setLoanApplication(loanApp);

		double income = loanApp.getCustomer().getMonthlyIncome();
		double loanAmount = loanApp.getLoanAmount();
		double dtiRatio = loanAmount / (income * 12);

		evaluation.setDebtToIncomeRatio(dtiRatio);
		evaluation.setApprovedAmount(loanAmount);
		evaluation.setInterestRate(10.5);
		evaluation.setEvaluationRemarks("Eligible based on verified documents and income.");
		evaluation.setEvaluationStatus("APPROVED");

		creditEvaluationRepository.save(evaluation);

		loanApp.setApplicationStatus(ApplicationStatus.EVALUATED);
		loanApp.setCreditEvaluation(evaluation);
		loanApplicationRepository.save(loanApp);

		loanStageHistoryService.logStage(loanApp.getId(), "Credit Manager", "CREDIT_MANAGER",
				ApplicationStatus.EVALUATED.name(), "Loan eligibility evaluated by credit manager.");
	}

	@Override
	public List<LoanApplicationforsanctionDTO> getEvaluatedApplications() {
		List<LoanApplication> applications = loanApplicationRepository
				.findByApplicationStatus(ApplicationStatus.EVALUATED);

		List<LoanApplicationforsanctionDTO> resultList = new ArrayList<>();

		for (LoanApplication application : applications) {
			LoanApplicationforsanctionDTO dto = new LoanApplicationforsanctionDTO();

			dto.setId(application.getId());
			dto.setLoanAmount(application.getLoanAmount());
			dto.setLoanTenureInMonths(application.getLoanTenureInMonths());
			dto.setCibilScore(application.getCibilScore());
			dto.setApplicationDate(application.getApplicationDate());

			if (application.getLoanPurpose() != null) {
				dto.setLoanPurpose(application.getLoanPurpose().toString());
			} else {
				dto.setLoanPurpose(null);
			}

			dto.setApplicationStatus(application.getApplicationStatus().toString());

			if (application.getCustomer() != null && application.getCustomer().getUser() != null) {
				dto.setApplicantName(application.getCustomer().getUser().getFullName());
			} else {
				dto.setApplicantName("Unknown");
			}

			if (application.getCreditEvaluation() != null) {
				CreditEvaluation eval = application.getCreditEvaluation();
				dto.setApprovedAmount(eval.getApprovedAmount());
				dto.setInterestRate(eval.getInterestRate());
				dto.setEvaluationRemarks(eval.getEvaluationRemarks());
			}

			resultList.add(dto);
		}

		return resultList;
	}

	@Override
	public SanctionLetter generateSanctionLetter(Long loanAppId, SanctionLetter input) {

		Optional<LoanApplication> loanOpt = loanApplicationRepository.findById(loanAppId);
		if (!loanOpt.isPresent()) {
			throw new RuntimeException("Loan Application not found with ID: " + loanAppId);
		}

		LoanApplication loanApp = loanOpt.get();

		SanctionLetter letter = new SanctionLetter();
		letter.setLoanApplication(loanApp);
		letter.setIssueDate(LocalDate.now());
		letter.setSanctionedAmount(input.getSanctionedAmount());
		letter.setInterestRate(input.getInterestRate());
		letter.setTenureInMonths(input.getTenureInMonths());
		letter.setEmiScheduleFileUrl(input.getEmiScheduleFileUrl());

		SanctionLetter savedLetter = sanctionLetterRepository.save(letter);

		loanApp.setSanctionLetter(savedLetter);
		loanApp.setApplicationStatus(ApplicationStatus.SANCTIONED);
		loanApplicationRepository.save(loanApp);

		LoanApplication app = loanApplicationRepository.findById(loanAppId).orElse(null);
		if (app != null) {
			app.setApplicationStatus(ApplicationStatus.SANCTIONED);
			loanApplicationRepository.save(app);

			loanStageHistoryService.logStage(app.getId(), "Credit Manager", "CREDIT_MANAGER",
					ApplicationStatus.SANCTIONED.name(), "Sanction letter issued.");
		}

		return savedLetter;

	}

	@Override
	public SanctionLetter getSanctionLetter(Long loanAppId) {
		return sanctionLetterRepository.findByLoanApplication_Id(loanAppId);
	}

}
