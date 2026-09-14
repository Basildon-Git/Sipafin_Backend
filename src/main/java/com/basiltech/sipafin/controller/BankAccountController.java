package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.BankAccountDtos;
import com.basiltech.sipafin.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<BankAccountDtos.BankAccountResponse>> createBankAccount(
            @Valid @RequestBody BankAccountDtos.CreateBankAccountRequest request
    ) {
        BankAccountDtos.BankAccountResponse response = bankAccountService.createBankAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(201, "Bank account created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankAccountDtos.BankAccountResponse>>> getAllBankAccounts() {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank accounts fetched successfully", bankAccountService.getAllBankAccounts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountDtos.BankAccountResponse>> getBankAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank account fetched successfully", bankAccountService.getBankAccountById(id)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BankAccountDtos.BankAccountResponse>>> getActiveBankAccounts() {
        return ResponseEntity.ok(ApiResponse.of(200, "Active bank accounts fetched successfully", bankAccountService.getActiveBankAccounts()));
    }

    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<BankAccountDtos.BankAccountResponse>>> getInactiveBankAccounts() {
        return ResponseEntity.ok(ApiResponse.of(200, "Inactive bank accounts fetched successfully", bankAccountService.getInactiveBankAccounts()));
    }

    @GetMapping("/bank/{bankId}")
    public ResponseEntity<ApiResponse<List<BankAccountDtos.BankAccountResponse>>> getBankAccountsByBank(@PathVariable Long bankId) {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank accounts by bank fetched successfully", bankAccountService.getBankAccountsByBank(bankId)));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<BankAccountDtos.BankAccountResponse>>> getBankAccountsByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank accounts by branch fetched successfully", bankAccountService.getBankAccountsByBranch(branchId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BankAccountDtos.BankAccountResponse>>> searchBankAccounts(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank account search completed successfully", bankAccountService.searchBankAccounts(keyword)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountDtos.BankAccountResponse>> updateBankAccount(
            @PathVariable Long id,
            @Valid @RequestBody BankAccountDtos.UpdateBankAccountRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank account updated successfully", bankAccountService.updateBankAccount(id, request)));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<BankAccountDtos.BankAccountResponse>> activateBankAccount(
            @PathVariable Long id,
            @Valid @RequestBody BankAccountDtos.BankAccountStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank account activated successfully", bankAccountService.activateBankAccount(id, request)));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<BankAccountDtos.BankAccountResponse>> deactivateBankAccount(
            @PathVariable Long id,
            @Valid @RequestBody BankAccountDtos.BankAccountStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Bank account deactivated successfully", bankAccountService.deactivateBankAccount(id, request)));
    }
}
