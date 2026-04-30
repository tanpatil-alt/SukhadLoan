package com.sit.homeloan.serviceimpl;

import com.sit.homeloan.dto.CustomerProfileDTO;

import com.sit.homeloan.dto.LoanApplicationDTO;
import com.sit.homeloan.dto.LoanApplicationRequestDTO;
import com.sit.homeloan.dto.SanctionDetailsDTO;
import com.sit.homeloan.enums.ApplicationStatus;
import com.sit.homeloan.enums.DocumentType;
import com.sit.homeloan.enums.VerificationStatus;
import com.sit.homeloan.model.Customer;
import com.sit.homeloan.model.Documents;
import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.model.SanctionLetter;
import com.sit.homeloan.model.User;
import com.sit.homeloan.repository.CustomerRepository;
import com.sit.homeloan.repository.DocumentRepository;
import com.sit.homeloan.repository.LoanApplicationRepository;
import com.sit.homeloan.service.CustomerService;
import com.sit.homeloan.service.LoanStageHistoryService;

import jakarta.transaction.Transactional;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private LoanStageHistoryService loanStageHistoryService;

//	private final String uploadDir = "C:/uploads";

	@Override
	public CustomerProfileDTO getCustomerProfileDTO(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email is required to fetch profile");
		}

		Optional<Customer> customerOpt = customerRepository.findByUserEmail(email);
		if (customerOpt.isEmpty()) {
			throw new IllegalArgumentException("Customer profile not found for email: " + email);
		}

		Customer customer = customerOpt.get();
		User user = customer.getUser();

		CustomerProfileDTO dto = new CustomerProfileDTO();
		dto.setFullName(user != null ? user.getFullName() : null);
		dto.setEmail(user != null ? user.getEmail() : null);
		dto.setPhoneNumber(user != null ? user.getPhoneNumber() : null);

		dto.setAddress(customer.getAddress());
		dto.setEmploymentType(customer.getEmploymentType());
		dto.setMonthlyIncome(customer.getMonthlyIncome());
		dto.setPanNumber(customer.getPanNumber());
		dto.setAadhaarNumber(customer.getAadhaarNumber());
		dto.setKycStatus(customer.getKycStatus());

		return dto;
	}

//	@Override
//	public String applyForLoan(LoanApplicationRequestDTO dto) {
//
//		Optional<Customer> customerOpt = customerRepository.findByUserEmail(dto.getEmail());
//
//		if (customerOpt.isEmpty()) {
//			return "Customer not found.";
//		}
//
//		Customer customer = customerOpt.get();
//
//		List<LoanApplication> existingApplications = loanApplicationRepository
//				.findByCustomer_User_Email(dto.getEmail());
//
//		if (!existingApplications.isEmpty()) {
//			return "You have already applied for a loan.";
//		}
//
//		if (customer.getPanNumber() == null || customer.getPanNumber().isEmpty()) {
//			customer.setPanNumber(dto.getPanNumber());
//		}
//		if (customer.getAadhaarNumber() == null || customer.getAadhaarNumber().isEmpty()) {
//			customer.setAadhaarNumber(dto.getAadhaarNumber());
//		}
//		customer.setAddress(dto.getAddress());
//		customer.setEmploymentType(dto.getEmploymentType());
//		customer.setEmployerName(dto.getEmployerName());
//		customer.setMonthlyIncome(dto.getMonthlyIncome());
//		customer.setBankAccountNumber(dto.getBankAccountNumber());
//		customer.setAccountHolderName(dto.getAccountHolderName());
//		customer.setIfscCode(dto.getIfscCode());
//		customerRepository.save(customer);
//
//		if (!dto.getPanNumber().equals(customer.getPanNumber())) {
//			return "PAN verification failed.";
//		}
//
//		double randomCibil = 650 + Math.random() * 200;
//
//		LoanApplication application = new LoanApplication();
//
//		application.setCustomer(customer);
//		application.setApplicationDate(LocalDate.now());
//		application.setApplicationStatus(ApplicationStatus.PENDING);
//		application.setCibilScore(randomCibil);
//		application.setLoanAmount(dto.getLoanAmount());
//		application.setLoanTenureInMonths(dto.getLoanTenureInMonths());
//		application.setLoanPurpose(dto.getLoanPurpose());
//
//		loanApplicationRepository.save(application);
//
//		loanStageHistoryService.logStage(application.getId(), customer.getUser().getFullName(),
//				customer.getUser().getRole().name(), ApplicationStatus.PENDING.name(),
//				"Loan applied with CIBIL: " + randomCibil);
//
//		return "Loan application submitted. CIBIL score: " + (int) randomCibil;
//	}

	@Transactional
	@Override
	public String applyForLoan(LoanApplicationRequestDTO dto) {

		if (dto == null || dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
			return "Invalid request: email is required.";
		}
		if (dto.getLoanAmount() == null || dto.getLoanAmount() <= 0) {
			return "Invalid loan amount.";
		}
		if (dto.getLoanTenureInMonths() == null || dto.getLoanTenureInMonths() <= 0) {
			return "Invalid loan tenure.";
		}

		Optional<Customer> customerOpt = customerRepository.findByUserEmail(dto.getEmail());
		if (!customerOpt.isPresent()) {
			return "Customer not found.";
		}
		Customer customer = customerOpt.get();

		List<LoanApplication> existingApplications = loanApplicationRepository
				.findByCustomer_User_Email(dto.getEmail());

		for (LoanApplication app : existingApplications) {
			ApplicationStatus status = app.getApplicationStatus();

			if (status != null && status != ApplicationStatus.REJECTED && status != ApplicationStatus.DISBURSED) {
				return "You already have an active loan application (ID: " + app.getId() + ").";
			}
		}

		if (dto.getPanNumber() != null && !dto.getPanNumber().trim().isEmpty()) {

			if (customer.getPanNumber() != null && !customer.getPanNumber().trim().isEmpty()
					&& !customer.getPanNumber().equals(dto.getPanNumber().trim())) {
				return "PAN verification failed: provided PAN does not match existing record.";
			} else if (customer.getPanNumber() == null || customer.getPanNumber().trim().isEmpty()) {
				customer.setPanNumber(dto.getPanNumber().trim());
			}
		}

		if (dto.getAadhaarNumber() != null && !dto.getAadhaarNumber().trim().isEmpty()) {
			if (customer.getAadhaarNumber() == null || customer.getAadhaarNumber().trim().isEmpty()) {
				customer.setAadhaarNumber(dto.getAadhaarNumber().trim());
			}
		}

		if (dto.getAddress() != null && !dto.getAddress().trim().isEmpty()) {
			customer.setAddress(dto.getAddress().trim());
		}
		if (dto.getEmploymentType() != null && !dto.getEmploymentType().trim().isEmpty()) {
			customer.setEmploymentType(dto.getEmploymentType().trim());
		}
		if (dto.getEmployerName() != null && !dto.getEmployerName().trim().isEmpty()) {
			customer.setEmployerName(dto.getEmployerName().trim());
		}
		if (dto.getMonthlyIncome() != null) {
			customer.setMonthlyIncome(dto.getMonthlyIncome());
		}
		if (dto.getBankAccountNumber() != null && !dto.getBankAccountNumber().trim().isEmpty()) {
			customer.setBankAccountNumber(dto.getBankAccountNumber().trim());
		}
		if (dto.getAccountHolderName() != null && !dto.getAccountHolderName().trim().isEmpty()) {
			customer.setAccountHolderName(dto.getAccountHolderName().trim());
		}
		if (dto.getIfscCode() != null && !dto.getIfscCode().trim().isEmpty()) {
			customer.setIfscCode(dto.getIfscCode().trim());
		}

		customerRepository.save(customer);

		double base = 650.0;

		double incomeContribution = 0.0;
		if (customer.getMonthlyIncome() != null) {
			incomeContribution = customer.getMonthlyIncome() / 1000.0;
			if (incomeContribution > 150) {
				incomeContribution = 150;
			}
		}

		double loanPenalty = dto.getLoanAmount() / 100000.0;
		if (loanPenalty > 200) {
			loanPenalty = 200;
		}

		double computedCibil = base + incomeContribution - loanPenalty;
		if (computedCibil < 300)
			computedCibil = 300;
		if (computedCibil > 900)
			computedCibil = 900;

		LoanApplication application = new LoanApplication();
		application.setCustomer(customer);
		application.setLoanAmount(dto.getLoanAmount());
		application.setLoanTenureInMonths(dto.getLoanTenureInMonths());
		application.setLoanPurpose(dto.getLoanPurpose());
		application.setApplicationDate(LocalDate.now());
		application.setCibilScore(computedCibil);
		application.setApplicationStatus(ApplicationStatus.PENDING);

		loanApplicationRepository.save(application);

		loanStageHistoryService.logStage(application.getId(), customer.getUser().getFullName(),
				customer.getUser().getRole().name(), application.getApplicationStatus().name(),
				"Loan applied. CIBIL: " + (int) computedCibil);

		return "Loan application submitted. Application ID: " + application.getId() + ". CIBIL score: "
				+ (int) computedCibil + ".";
	}

	@Override
	public List<LoanApplicationDTO> getMyLoanApplications(String email) {
		List<LoanApplication> applications = loanApplicationRepository.findByCustomer_User_Email(email);

		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email is required to fetch applications");
		}

		List<LoanApplicationDTO> dtoList = new ArrayList<>();

		for (LoanApplication app : applications) {
			LoanApplicationDTO dto = new LoanApplicationDTO();
			dto.setId(app.getId());
			dto.setApplicantName(app.getCustomer().getUser().getFullName());
			dto.setLoanAmount(app.getLoanAmount());
			dto.setLoanTenureInMonths(app.getLoanTenureInMonths());
			dto.setLoanPurpose(app.getLoanPurpose());
			dto.setApplicationStatus(app.getApplicationStatus().name());
			dto.setApplicationDate(app.getApplicationDate());
			dto.setCibilScore(app.getCibilScore());

			dtoList.add(dto);

		}

		return dtoList;
	}

	@Override
	public String uploadDocument(MultipartFile file, String email, DocumentType documentType) {
		Optional<Customer> customerOpt = customerRepository.findByUserEmail(email);

		if (!customerOpt.isPresent()) {
			return "Customer not found!";
		}

		try {

			byte[] fileBytes = file.getBytes();

			Documents doc = new Documents();
			doc.setCustomer(customerOpt.get());
			doc.setFileData(fileBytes);
			doc.setFileName(file.getOriginalFilename());
			doc.setFileType(file.getContentType());
			doc.setUploadDate(LocalDate.now());
			doc.setDocumentType(documentType);
			doc.setVerificationStatus(VerificationStatus.PENDING);

			documentRepository.save(doc);

			List<LoanApplication> applications = loanApplicationRepository.findByCustomer_User_Email(email);

			if (applications != null && !applications.isEmpty()) {

				LoanApplication latest = applications.get(applications.size() - 1);

				List<Documents> uploadedDocs = documentRepository.findByCustomer_User_Email(email);

				Set<DocumentType> uploadedTypes = new HashSet<>();

				for (Documents d : uploadedDocs) {
					uploadedTypes.add(d.getDocumentType());
				}

				if (!uploadedTypes.isEmpty()) {
					if (latest.getApplicationStatus().ordinal() < ApplicationStatus.DOCUMENT_SUBMITTED.ordinal()) {
						latest.setApplicationStatus(ApplicationStatus.DOCUMENT_SUBMITTED);
					}
				}

				loanApplicationRepository.save(latest);
			}

			return "Document uploaded successfully!";
		} catch (IOException e) {
			return "File upload failed!";
		}
	}

	@Override
	public ResponseEntity<Resource> downloadDocument(String fileName) {
		List<Documents> documents = documentRepository.findAll();

		for (Documents doc : documents) {
			if (doc.getFileName().equals(fileName)) {
				ByteArrayResource resource = new ByteArrayResource(doc.getFileData());

				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
						.header(HttpHeaders.CONTENT_TYPE, doc.getFileType()).body(resource);
			}
		}

		return ResponseEntity.notFound().build();
	}

	@Override
	public List<Documents> getMyDocuments(String email) {
		return documentRepository.findByCustomer_User_Email(email);
	}

	@Override
	public String deleteLoanApplication(Long applicationId, String email) {
		Optional<LoanApplication> applicationOpt = loanApplicationRepository.findById(applicationId);

		if (applicationOpt.isEmpty()) {
			return "Loan application not found.";
		}

		LoanApplication application = applicationOpt.get();

		if (!application.getCustomer().getUser().getEmail().equalsIgnoreCase(email)) {
			return "Unauthorized deletion attempt.";
		}

		loanApplicationRepository.delete(application);
		return "Loan application deleted successfully.";
	}

	@Override
	public SanctionDetailsDTO getSanctionDetailsByEmailAndLoanId(String email, Long loanAppId) {
		Optional<Customer> custOpt = customerRepository.findByUserEmail(email);
		if (!custOpt.isPresent()) {
			return null;
		}
		Customer customer = custOpt.get();

		Optional<LoanApplication> loanOpt = loanApplicationRepository.findById(loanAppId);
		if (!loanOpt.isPresent()) {
			return null;
		}
		LoanApplication loan = loanOpt.get();

		if (!loan.getCustomer().getId().equals(customer.getId())) {
			return null;
		}

		SanctionLetter sanctionLetter = loan.getSanctionLetter();
		if (sanctionLetter == null) {
			return null;
		}

		SanctionDetailsDTO dto = new SanctionDetailsDTO();
		dto.setLoanAppId(loan.getId());
		dto.setLoanAmount(loan.getLoanAmount());
		dto.setLoanTenureInMonths(loan.getLoanTenureInMonths());
		dto.setLoanPurpose(loan.getLoanPurpose());

		dto.setApplicantName(customer.getUser().getFullName());
		dto.setApplicantEmail(customer.getUser().getEmail());
		dto.setApplicantPhone(customer.getUser().getPhoneNumber());

		dto.setSanctionedAmount(sanctionLetter.getSanctionedAmount());
		dto.setInterestRate(sanctionLetter.getInterestRate());
		dto.setTenureInMonths(sanctionLetter.getTenureInMonths());
		dto.setIssueDate(sanctionLetter.getIssueDate());
		dto.setBankName(sanctionLetter.getBankName());
		dto.setBankBranch(sanctionLetter.getBankBranch());

		return dto;
	}

	@Override
	public ResponseEntity<Resource> downloadSanctionLetterPdfByEmailAndLoanId(String email, Long loanAppId) {
		SanctionDetailsDTO dto = getSanctionDetailsByEmailAndLoanId(email, loanAppId);
		if (dto == null) {
			return ResponseEntity.notFound().build();
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			PdfWriter writer = new PdfWriter(baos);
			PdfDocument pdfDoc = new PdfDocument(writer);
			Document document = new Document(pdfDoc);

			document.add(new Paragraph("Sanction Letter").setBold().setFontSize(18).setMarginBottom(20));

			float[] columnWidths = { 150F, 300F };
			Table table = new Table(columnWidths);

			table.addCell(new Cell().add(new Paragraph("Loan Application ID")));
			table.addCell(new Cell().add(new Paragraph(String.valueOf(dto.getLoanAppId()))));

			table.addCell(new Cell().add(new Paragraph("Loan Amount")));
			table.addCell(new Cell().add(new Paragraph("₹" + dto.getLoanAmount())));

			table.addCell(new Cell().add(new Paragraph("Loan Tenure (Months)")));
			table.addCell(new Cell().add(new Paragraph(String.valueOf(dto.getLoanTenureInMonths()))));

			table.addCell(new Cell().add(new Paragraph("Loan Purpose")));
			table.addCell(new Cell().add(new Paragraph(dto.getLoanPurpose())));

			table.addCell(new Cell().add(new Paragraph("Applicant Name")));
			table.addCell(new Cell().add(new Paragraph(dto.getApplicantName())));

			table.addCell(new Cell().add(new Paragraph("Applicant Email")));
			table.addCell(new Cell().add(new Paragraph(dto.getApplicantEmail())));

			table.addCell(new Cell().add(new Paragraph("Applicant Phone")));
			table.addCell(new Cell().add(new Paragraph(dto.getApplicantPhone())));

			table.addCell(new Cell().add(new Paragraph("Sanctioned Amount")));
			table.addCell(new Cell().add(new Paragraph("₹" + dto.getSanctionedAmount())));

			table.addCell(new Cell().add(new Paragraph("Interest Rate (%)")));
			table.addCell(new Cell().add(new Paragraph(String.valueOf(dto.getInterestRate()))));

			table.addCell(new Cell().add(new Paragraph("Tenure (Months)")));
			table.addCell(new Cell().add(new Paragraph(String.valueOf(dto.getTenureInMonths()))));

			table.addCell(new Cell().add(new Paragraph("Issue Date")));
			table.addCell(new Cell().add(new Paragraph(dto.getIssueDate().toString())));

			table.addCell(new Cell().add(new Paragraph("Bank Name")));
			table.addCell(new Cell().add(new Paragraph(dto.getBankName())));

			table.addCell(new Cell().add(new Paragraph("Bank Branch")));
			table.addCell(new Cell().add(new Paragraph(dto.getBankBranch())));

			document.add(table);
			document.close();

			ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

			return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"sanction_letter_" + loanAppId + ".pdf\"").body(resource);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

}
