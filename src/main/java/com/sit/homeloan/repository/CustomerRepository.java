package com.sit.homeloan.repository;

import com.sit.homeloan.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
    Optional<Customer> findByUserEmail(String email);
}
