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
    public ResponseEntity<ApiResponse<?>> startReconciliation(
            @Valid @RequestBody BankReconciliationDtos.StartBankReconciliationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Bank reconciliation started successfully",
                bankReconciliationService.startReconciliation(request)
        ));
    }

    @PostMapping("/{reconciliationId}/items")
    public ResponseEntity<ApiResponse<?>> addReconciliationItem(
            @PathVariable Long reconciliationId,
            @Valid @RequestBody BankReconciliationDtos.AddReconciliationItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Bank reconciliation item posted successfully",
                bankReconciliationService.addReconciliationItem(reconciliationId, request)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getReconciliationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank reconciliation fetched successfully",
                bankReconciliationService.getReconciliationById(id)
        ));
    }

    @GetMapping("/{reconciliationId}/items")
    public ResponseEntity<ApiResponse<?>> getReconciliationItems(@PathVariable Long reconciliationId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank reconciliation items fetched successfully",
                bankReconciliationService.getReconciliationItems(reconciliationId)
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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllReconciliations() {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank reconciliations fetched successfully",
                bankReconciliationService.getAllReconciliations()
        ));
    }
}