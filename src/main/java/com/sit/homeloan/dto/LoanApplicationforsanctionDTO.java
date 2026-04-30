package com.sit.homeloan.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class LoanApplicationforsanctionDTO {
	private Long id;
    private String applicantName;
    private Double loanAmount;
    private Integer loanTenureInMonths;
    private double cibilScore;
    private LocalDate applicationDate;
    private String loanPurpose;
    private String applicationStatus;
    
  
    private Double approvedAmount;
    private Double interestRate;
    private String evaluationRemarks;
}
