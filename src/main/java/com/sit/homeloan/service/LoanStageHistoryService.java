package com.sit.homeloan.service;

import java.util.List;
import com.sit.homeloan.model.LoanStageHistory;

public interface LoanStageHistoryService {
    void logStage(Long appId, String updatedBy, String role, String stage, String remarks);
    List<LoanStageHistory> getLoanHistory(Long appId);
}
