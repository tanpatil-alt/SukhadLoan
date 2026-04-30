package com.sit.homeloan.dto;

import java.time.LocalDate;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationDTO {
    private Long id;
    private String applicantName;
    private Double loanAmount;
    private Integer loanTenureInMonths;
    private String loanPurpose;
    private String applicationStatus;
    private LocalDate applicationDate;
    private Double cibilScore;
    
}


