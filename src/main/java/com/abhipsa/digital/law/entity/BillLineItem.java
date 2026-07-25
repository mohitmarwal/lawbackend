package com.abhipsa.digital.law.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BillLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String purpose;

    // LUMPSUM | HOURLY
    private String billType;

    private Double hours;

    private Double rate;

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    @JsonIgnore
    private Bill bill;

    public void calculate() {
        if ("HOURLY".equalsIgnoreCase(billType) && hours != null && rate != null) {
            amount = hours * rate;
        }
    }
}
