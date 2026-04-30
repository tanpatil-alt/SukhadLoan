package com.sit.homeloan.serviceimpl;

import com.sit.homeloan.dto.ApiResponse;
import com.sit.homeloan.dto.LoginRequest;
import com.sit.homeloan.dto.RegisterRequest;
import com.sit.homeloan.enums.Role;
import com.sit.homeloan.model.Customer;
import com.sit.homeloan.model.User;
import com.sit.homeloan.repository.CustomerRepository;
import com.sit.homeloan.repository.UserRepository;
import com.sit.homeloan.service.AuthService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public ApiResponse registerUser(RegisterRequest request) {

		Optional<User> existing = userRepository.findByEmail(request.getEmail());
		if (existing.isPresent()) {
			return new ApiResponse(false, "User with this email already exists.");
		}

		Role role;
		try {
			role = Role.valueOf(request.getRole().toUpperCase());
		} catch (IllegalArgumentException e) {
			return new ApiResponse(false, "Invalid role: " + request.getRole());
		}

		if (role == Role.LOAN_OFFICER || role == Role.CREDIT_MANAGER || role == Role.DISBURSEMENT_MANAGER) {
			if (userRepository.existsByRole(role)) {
				return new ApiResponse(false, "A user with role " + role.name() + " is already registered.");
			}
		}

		User user = new User();
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setPhoneNumber(request.getPhoneNumber());
		user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
		user.setRole(role);

		User savedUser = userRepository.save(user);

		if (role == Role.CUSTOMER) {
			Customer customer = new Customer();
			customer.setUser(savedUser);
			customerRepository.save(customer);
		}

		return new ApiResponse(true, "User registered successfully as " + role.name());
	}

	@Override
	public ApiResponse loginUser(LoginRequest request) {
		Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

		if (userOpt.isEmpty()) {
			return new ApiResponse(false, "Invalid credentials!");
		}

		User user = userOpt.get();

		if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
			return new ApiResponse(false, "Invalid credentials!");
		}

		return new ApiResponse(true, "Login successful for role: " + user.getRole().name());
	}
}
