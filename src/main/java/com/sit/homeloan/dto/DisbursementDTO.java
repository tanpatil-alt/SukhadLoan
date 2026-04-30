package com.sit.homeloan.dto;

import java.time.LocalDate;

import lombok.Data;
@Data
public class DisbursementDTO {
    private Long id;
    private Long loanAppId;
    private String applicantName;
    private Double loanAmount;
    private Double approvedAmount;
    private Double disbursedAmount;
    private LocalDate disbursementDate;
    private String disbursementStatus;
    
}
