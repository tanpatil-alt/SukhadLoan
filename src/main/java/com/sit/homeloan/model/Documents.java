package com.sit.homeloan.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.sit.homeloan.enums.DocumentType;
import com.sit.homeloan.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Documents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Customer customer;
// private String fileUrl;
    
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Column(name = "file_type")
    private String fileType;

    private LocalDate uploadDate;
    
    @Column(name = "file_name")
    private String fileName;


    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
}
