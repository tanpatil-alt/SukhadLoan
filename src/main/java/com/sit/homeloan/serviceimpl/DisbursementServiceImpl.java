package com.sit.homeloan.serviceimpl;

import com.sit.homeloan.dto.DisbursementDTO;
import com.sit.homeloan.dto.SanctionedLoanDTO;
import com.sit.homeloan.enums.ApplicationStatus;
import com.sit.homeloan.model.*;
import com.sit.homeloan.repository.*;
import com.sit.homeloan.service.DisbursementService;
import com.sit.homeloan.service.LoanStageHistoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DisbursementServiceImpl implements DisbursementService {

	@Autowired
	private DisbursementRepository disbursementRepository;

	@Autowired
	private LoanStageHistoryService loanStageHistoryService;

	@Autowired
	private SanctionLetterRepository sanctionLetterRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Override
	public String disburseLoan(Long loanAppId, Double amount) {
		Optional<LoanApplication> loanAppOpt = loanApplicationRepository.findById(loanAppId);
		if (loanAppOpt.isEmpty()) {
			return "Loan Application not found with ID: " + loanAppId;
		}

		LoanApplication loanApp = loanAppOpt.get();

		if (disbursementRepository.findByLoanApplication(loanApp).isPresent()) {
			return "Disbursement already exists for this Loan Application.";
		}

		SanctionLetter sanction = sanctionLetterRepository.findByLoanApplication_Id(loanAppId);
		if (sanction == null) {
			return "No sanction letter found for this Loan Application.";
		}
		if (amount > sanction.getSanctionedAmount()) {
			return "Disbursement amount cannot exceed sanctioned amount.";
		}

		Disbursement disbursement = new Disbursement();
		disbursement.setLoanApplication(loanApp);
		disbursement.setDisbursedAmount(amount);
		disbursement.setDisbursementDate(LocalDate.now());
		disbursement.setDisbursementStatus("DISBURSED");
		disbursementRepository.save(disbursement);

		loanApp.setApplicationStatus(ApplicationStatus.DISBURSED);
		loanApplicationRepository.save(loanApp);

		loanStageHistoryService.logStage(loanApp.getId(), "Disbursement Manager", "DISBURSEMENT_MANAGER",
				ApplicationStatus.DISBURSED.name(), "Loan disbursed successfully.");

		return "Loan Disbursed Successfully for LoanApp ID: " + loanAppId;
	}

	@Override
	public ResponseEntity<?> getByLoanAppId(Long loanAppId) {
		Optional<LoanApplication> loanAppOpt = loanApplicationRepository.findById(loanAppId);
		if (loanAppOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loan Application not found");
		}

		Optional<Disbursement> disbOpt = disbursementRepository.findByLoanApplication(loanAppOpt.get());
		if (disbOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("No disbursement found for LoanApp ID: " + loanAppId);
		}

		return ResponseEntity.ok(disbOpt.get());
	}

	@Override
	public Disbursement getDisbursementObjectByLoanAppId(Long loanAppId) {
		Optional<LoanApplication> loanAppOpt = loanApplicationRepository.findById(loanAppId);
		if (loanAppOpt.isEmpty())
			return null;

		return disbursementRepository.findByLoanApplication(loanAppOpt.get()).orElse(null);
	}

	@Override
	public List<SanctionedLoanDTO> getSanctionedApplications() {
		List<LoanApplication> sanctionedApps = loanApplicationRepository
				.findByApplicationStatus(ApplicationStatus.SANCTIONED);

		List<SanctionedLoanDTO> result = new ArrayList<>();

		for (LoanApplication app : sanctionedApps) {
			SanctionedLoanDTO dto = new SanctionedLoanDTO();
			dto.setLoanAppId(app.getId());

			if (app.getCustomer() != null && app.getCustomer().getUser() != null) {
				dto.setApplicantName(app.getCustomer().getUser().getFullName());
			}

			dto.setLoanAmount(app.getLoanAmount());

			SanctionLetter sanction = sanctionLetterRepository.findByLoanApplication_Id(app.getId());

			if (sanction != null) {
				dto.setApprovedAmount(sanction.getSanctionedAmount());
				dto.setInterestRate(sanction.getInterestRate());
				dto.setTenureMonths(sanction.getTenureInMonths());
				dto.setSanctionDate(sanction.getIssueDate());
			}

			result.add(dto);
		}

		return result;
	}

	@Override
	public SanctionedLoanDTO getSanctionedApplicationDetails(Long loanAppId) {
		Optional<LoanApplication> appOpt = loanApplicationRepository.findById(loanAppId);
		if (appOpt.isEmpty() || appOpt.get().getApplicationStatus() != ApplicationStatus.SANCTIONED) {
			return null;
		}

		LoanApplication app = appOpt.get();
		SanctionedLoanDTO dto = new SanctionedLoanDTO();

		dto.setLoanAppId(app.getId());

		if (app.getCustomer() != null && app.getCustomer().getUser() != null) {
			dto.setApplicantName(app.getCustomer().getUser().getFullName());
		}

		dto.setLoanAmount(app.getLoanAmount());

		SanctionLetter sanction = sanctionLetterRepository.findByLoanApplication_Id(app.getId());

		if (sanction != null) {
			dto.setApprovedAmount(sanction.getSanctionedAmount());
			dto.setInterestRate(sanction.getInterestRate());
			dto.setTenureMonths(sanction.getTenureInMonths());
			dto.setSanctionDate(sanction.getIssueDate());
		}

		return dto;
	}

	@Override
	public List<DisbursementDTO> getProcessedDisbursements() {
	    List<LoanApplication> loanApps = loanApplicationRepository.findByApplicationStatus(ApplicationStatus.DISBURSED);
	    List<DisbursementDTO> dtos = new ArrayList<>();
	    for (LoanApplication loan : loanApps) {
	        DisbursementDTO dto = new DisbursementDTO();
	        dto.setId(loan.getId());
	        dto.setLoanAppId(loan.getId());
	        dto.setApplicantName(loan.getCustomer().getUser().getFullName()); 
	        dto.setLoanAmount(loan.getLoanAmount());
	        dto.setApprovedAmount(loan.getSanctionLetter().getSanctionedAmount()); 
	        dto.setDisbursedAmount(loan.getDisbursement().getDisbursedAmount()); 
	        dto.setDisbursementDate(loan.getDisbursement().getDisbursementDate());
	        dto.setDisbursementStatus(loan.getApplicationStatus().name());
	        dtos.add(dto);
	    }
	    return dtos;
	}

}
