package com.sit.homeloan.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loan_stage_histories")
public class LoanStageHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    private LoanApplication loanApplication;

    private String updatedByRole;
    private String updatedByName;
    private String stage;
    private String remarks;
    private LocalDateTime updatedAt;
}
