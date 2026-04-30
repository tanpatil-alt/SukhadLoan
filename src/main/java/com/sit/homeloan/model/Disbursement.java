package com.sit.homeloan.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "disbursements")
public class Disbursement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private LoanApplication loanApplication;
    private Double disbursedAmount;
    private LocalDate disbursementDate;
    private String disbursementStatus;
}
