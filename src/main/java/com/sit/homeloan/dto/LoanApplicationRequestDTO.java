package com.sit.homeloan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequestDTO {
    private String email;
    private String panNumber;
    private String aadhaarNumber;
    private String address;
    private String employmentType;
    private String employerName;
    private Double monthlyIncome;
    private Double loanAmount;
    private Integer loanTenureInMonths;
    private String loanPurpose;
    private String bankAccountNumber;
    private String ifscCode;
    private String accountHolderName;

}
