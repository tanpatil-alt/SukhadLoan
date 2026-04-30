package com.sit.homeloan.repository;

import com.sit.homeloan.model.Documents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Documents, Long> {
    List<Documents> findByCustomer_User_Email(String email);
    
    List<Documents> findByCustomerId(Long customerId);
}

