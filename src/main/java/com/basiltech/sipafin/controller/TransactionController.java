package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.TransactionDtos;
import com.basiltech.sipafin.service.TransactionPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionPostingService transactionPostingService;

    @PostMapping("/cash/opening-balance")
    public ResponseEntity<ApiResponse<TransactionDtos.CashTransactionResponse>> openCashFloat(
            @Valid @RequestBody TransactionDtos.CashOpeningBalanceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Cash float opening balance posted successfully",
                        transactionPostingService.openCashFloat(request)
                ));
    }

    @PostMapping("/bank-deposit")
    public ResponseEntity<ApiResponse<TransactionDtos.TransactionGroupResponse>> depositCashToBank(
            @Valid @RequestBody TransactionDtos.BankDepositRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Cash deposited to bank successfully",
                        transactionPostingService.depositCashToBank(request)
                ));
    }

    @PostMapping("/bank-withdrawal")
    public ResponseEntity<ApiResponse<TransactionDtos.TransactionGroupResponse>> withdrawBankToCash(
            @Valid @RequestBody TransactionDtos.BankWithdrawalRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Bank withdrawal to cash float posted successfully",
                        transactionPostingService.withdrawBankToCash(request)
                ));
    }

    @PostMapping("/branch-transfer")
    public ResponseEntity<ApiResponse<TransactionDtos.BranchTransferResponse>> transferCashBetweenBranches(
            @Valid @RequestBody TransactionDtos.BranchTransferRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Branch cash transfer posted successfully",
                        transactionPostingService.transferCashBetweenBranches(request)
                ));
    }

    @PostMapping("/cash-adjustment")
    public ResponseEntity<ApiResponse<TransactionDtos.CashTransactionResponse>> adjustCashFloat(
            @Valid @RequestBody TransactionDtos.CashAdjustmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Cash float adjustment posted successfully",
                        transactionPostingService.adjustCashFloat(request)
                ));
    }

    @PostMapping("/bank-adjustment")
    public ResponseEntity<ApiResponse<TransactionDtos.BankTransactionResponse>> adjustBankBalance(
            @Valid @RequestBody TransactionDtos.BankAdjustmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Bank balance adjustment posted successfully",
                        transactionPostingService.adjustBankBalance(request)
                ));
    }

    @GetMapping("/cash")
    public ResponseEntity<ApiResponse<?>> getAllCashTransactions() {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Cash transactions fetched successfully",
                transactionPostingService.getAllCashTransactions()
        ));
    }

    @GetMapping("/bank")
    public ResponseEntity<ApiResponse<?>> getAllBankTransactions() {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank transactions fetched successfully",
                transactionPostingService.getAllBankTransactions()
        ));
    }

    @GetMapping("/cash/branch/{branchId}")
    public ResponseEntity<ApiResponse<?>> getCashTransactionsByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch cash transactions fetched successfully",
                transactionPostingService.getCashTransactionsByBranch(branchId)
        ));
    }

    @GetMapping("/bank/account/{bankAccountId}")
    public ResponseEntity<ApiResponse<?>> getBankTransactionsByAccount(@PathVariable Long bankAccountId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank account transactions fetched successfully",
                transactionPostingService.getBankTransactionsByAccount(bankAccountId)
        ));
    }

    @GetMapping("/group/{transactionGroupId}")
    public ResponseEntity<ApiResponse<TransactionDtos.TransactionGroupResponse>> getTransactionGroup(
            @PathVariable String transactionGroupId
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Transaction group fetched successfully",
                transactionPostingService.getTransactionGroup(transactionGroupId)
        ));
    }
}