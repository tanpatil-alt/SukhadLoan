package com.sit.homeloan.serviceimpl;

import com.sit.homeloan.model.LoanApplication;
import com.sit.homeloan.model.LoanStageHistory;
import com.sit.homeloan.repository.LoanApplicationRepository;
import com.sit.homeloan.repository.LoanStageHistoryRepository;
import com.sit.homeloan.service.LoanStageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanStageHistoryServiceImpl implements LoanStageHistoryService {

	@Autowired
	private LoanStageHistoryRepository loanStageHistoryRepository;

	@Autowired
	private LoanApplicationRepository loanApplicationRepository;

	@Override
	public void logStage(Long appId, String updatedBy, String role, String stage, String remarks) {
		LoanStageHistory history = new LoanStageHistory();
		history.setLoanApplication(loanApplicationRepository.findById(appId).get());
		history.setUpdatedByName(updatedBy);
		history.setUpdatedByRole(role);
		history.setStage(stage);
		history.setRemarks(remarks);
		history.setUpdatedAt(LocalDateTime.now());
		loanStageHistoryRepository.save(history);
	}

	@Override
	public List<LoanStageHistory> getLoanHistory(Long appId) {
		return loanStageHistoryRepository.findByLoanApplicationId(appId);
	}
}
