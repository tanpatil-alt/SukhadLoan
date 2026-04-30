package com.sit.homeloan.dto;

import lombok.Data;

@Data
public class CustomerProfileDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String employmentType;
    private Double monthlyIncome;
    private String panNumber;
    private String aadhaarNumber;
    private String kycStatus;
}
