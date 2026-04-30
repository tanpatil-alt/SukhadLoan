package com.sit.homeloan.service;

import com.sit.homeloan.dto.ApiResponse;
import com.sit.homeloan.dto.LoginRequest;
import com.sit.homeloan.dto.RegisterRequest;
import com.sit.homeloan.model.User;

public interface AuthService {
	 ApiResponse registerUser(RegisterRequest request);
	    ApiResponse loginUser(LoginRequest request);
}