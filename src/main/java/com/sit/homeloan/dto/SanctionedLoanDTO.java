package com.sit.homeloan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SanctionedLoanDTO {
    private Long loanAppId;
    private String applicantName;
    private Double loanAmount;
    private Double approvedAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private LocalDate sanctionDate;

}