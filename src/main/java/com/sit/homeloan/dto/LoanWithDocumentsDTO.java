package com.sit.homeloan.dto;

import java.util.List;


import com.sit.homeloan.model.Documents;
import com.sit.homeloan.model.LoanApplication;

import lombok.Data;
@Data
public class LoanWithDocumentsDTO {

    private LoanApplication loan;
    private List<DocumentDto> documents;;
}
