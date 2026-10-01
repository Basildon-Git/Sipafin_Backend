package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.CommissionDtos;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.service.CommissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/commissions")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<?>> createCommissionAccount(
            @Valid @RequestBody CommissionDtos.CreateBankCommissionAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Commission account created successfully",
                commissionService.createCommissionAccount(request)
        ));
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<?>> updateCommissionAccount(
            @PathVariable Long id,
            @Valid @RequestBody CommissionDtos.UpdateBankCommissionAccountRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Commission account updated successfully",
                commissionService.updateCommissionAccount(id, request)
        ));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<?>> getAllCommissionAccounts() {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Commission accounts fetched successfully",
                commissionService.getAllCommissionAccounts()
        ));
    }

    @GetMapping("/accounts/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getCommissionAccountsByCurrency(@PathVariable CurrencyCode currency) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Commission accounts by currency fetched successfully",
                commissionService.getCommissionAccountsByCurrency(currency)
        ));
    }

    @GetMapping("/accounts/bank-account/{bankAccountId}")
    public ResponseEntity<ApiResponse<?>> getCommissionAccountByBankAccount(@PathVariable Long bankAccountId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Commission account fetched successfully",
                commissionService.getCommissionAccountByBankAccount(bankAccountId)
        ));
    }

    @GetMapping("/branch/{branchId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getCommissionsByBranchAndCurrency(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch commissions fetched successfully",
                commissionService.getCommissionsByBranchAndCurrency(branchId, currency)
        ));
    }

    @GetMapping("/branch/{branchId}/currency/{currency}/date/{commissionDate}")
    public ResponseEntity<ApiResponse<?>> getCommissionsByBranchCurrencyAndDate(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate commissionDate
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch commissions by date fetched successfully",
                commissionService.getCommissionsByBranchCurrencyAndDate(branchId, currency, commissionDate)
        ));
    }
}