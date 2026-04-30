package com.sit.homeloan.repository;

import com.sit.homeloan.model.LoanStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanStageHistoryRepository extends JpaRepository<LoanStageHistory, Long> {
    
    List<LoanStageHistory> findByLoanApplicationId(Long loanApplicationId);
}
