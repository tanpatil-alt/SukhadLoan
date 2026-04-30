package com.sit.homeloan.repository;

import com.sit.homeloan.model.SanctionLetter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SanctionLetterRepository extends JpaRepository<SanctionLetter, Long> {
    SanctionLetter findByLoanApplication_Id(Long loanApplicationId);
}
