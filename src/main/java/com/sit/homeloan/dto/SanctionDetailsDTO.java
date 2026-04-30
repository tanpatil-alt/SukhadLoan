package com.sit.homeloan.dto;

import java.time.LocalDate;

import lombok.Data;
@Data
public class SanctionDetailsDTO {
	private Long loanAppId;
	private Double loanAmount;
	private Integer loanTenureInMonths;
	private String loanPurpose;

	private String applicantName;
	private String applicantEmail;
	private String applicantPhone;

	private Double sanctionedAmount;
	private Double interestRate;
	private Integer tenureInMonths;
	private LocalDate issueDate;

	private String bankName;
	private String bankBranch;
}
