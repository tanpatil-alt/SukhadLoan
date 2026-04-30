package com.sit.homeloan.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_evaluations")
public class CreditEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private LoanApplication loanApplication;

    private Double debtToIncomeRatio;
    private Double approvedAmount;
    private Double interestRate;
    private String evaluationRemarks;
    private String evaluationStatus;
}
