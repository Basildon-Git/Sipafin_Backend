package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.BankReconciliationDtos;
import com.basiltech.sipafin.service.BankReconciliationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bank-reconciliations")
@RequiredArgsConstructor
public class BankReconciliationController {

    private final BankReconciliationService bankReconciliationService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> reconcile(
            @Valid @RequestBody BankReconciliationDtos.ReconcileBankAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Bank reconciliation posted successfully",
                bankReconciliationService.reconcile(request)
        ));
    }

    @GetMapping("/bank-account/{bankAccountId}")
    public ResponseEntity<ApiResponse<?>> getReconciliationsByBankAccount(@PathVariable Long bankAccountId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank reconciliations fetched successfully",
                bankReconciliationService.getReconciliationsByBankAccount(bankAccountId)
        ));
    }
}