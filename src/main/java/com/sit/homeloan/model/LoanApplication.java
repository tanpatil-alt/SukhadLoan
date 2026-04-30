package com.sit.homeloan.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sit.homeloan.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loan_applications")
public class LoanApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference(value = "customer-loanApplications") 
    @JsonIgnore
    @JsonIgnoreProperties("loanApplications")
    private Customer customer;


    private Double loanAmount;
    private Integer loanTenureInMonths;
    private String loanPurpose;
    private LocalDate applicationDate;
    private Double cibilScore;
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @OneToOne(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    private CreditEvaluation creditEvaluation;

    @OneToOne(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    private SanctionLetter sanctionLetter;

    @OneToOne(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    private Disbursement disbursement;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL)
    private List<LoanStageHistory> loanStageHistories;
}