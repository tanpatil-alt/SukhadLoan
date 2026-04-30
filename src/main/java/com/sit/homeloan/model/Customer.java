package com.sit.homeloan.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class Customer {

	@Id
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "user_id")
	@JsonManagedReference(value = "user-customer")
	private User user;

	private String address;
	private String employmentType;
	private String employerName;
	private Double monthlyIncome;
	private String panNumber;
	private String aadhaarNumber;
	private String kycStatus;
	private String bankAccountNumber;
	private String ifscCode;
	private String accountHolderName;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
//    @JsonManagedReference(value = "customer-loanApplications")
	private List<LoanApplication> loanApplications;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Documents> documents;
}
