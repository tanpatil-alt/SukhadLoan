package com.sit.homeloan.dto;

import com.sit.homeloan.enums.DocumentType;

import lombok.Data;

@Data
public class DocumentDto {
	 private Long id;
	    private String name;
	    private String documentType;
	    private String verificationStatus;
	    private String fileType;  
	    private String data;
}
