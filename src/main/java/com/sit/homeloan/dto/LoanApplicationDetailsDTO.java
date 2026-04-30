package com.sit.homeloan.dto;

import lombok.Data;

@Data
public class LoanApplicationDetailsDTO {
    private Long applicationId;
    private String fullName;
    private String email;
    private String panNumber;
    private String aadhaarNumber;
    private String address;
    private String employmentType;
    private String employerName;
    private Double monthlyIncome;
    private String bankAccountNumber;
    private String accountHolderName;
    private String ifscCode;
    private String loanPurpose;
    private Double loanAmount;
    private Integer loanTenureInMonths;
    private String applicationDate;

    
}

