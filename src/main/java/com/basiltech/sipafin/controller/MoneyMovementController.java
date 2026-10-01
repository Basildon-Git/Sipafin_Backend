package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.MoneyMovementDtos;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.service.MoneyMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/money-movements")
@RequiredArgsConstructor
public class MoneyMovementController {

    private final MoneyMovementService moneyMovementService;

    @PostMapping("/float/opening-balance")
    public ResponseEntity<ApiResponse<?>> openFloat(
            @Valid @RequestBody MoneyMovementDtos.CashOpeningBalanceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Float opening balance posted successfully",
                moneyMovementService.openFloat(request)
        ));
    }

    @PostMapping("/float-to-bank")
    public ResponseEntity<ApiResponse<?>> depositFloatToBank(
            @Valid @RequestBody MoneyMovementDtos.BankDepositRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Float deposited to bank successfully",
                moneyMovementService.depositFloatToBank(request)
        ));
    }

    @PostMapping("/bank-to-float")
    public ResponseEntity<ApiResponse<?>> withdrawBankToFloat(
            @Valid @RequestBody MoneyMovementDtos.BankWithdrawalRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Bank withdrawal to float posted successfully",
                moneyMovementService.withdrawBankToFloat(request)
        ));
    }

    @PostMapping("/float-transfer")
    public ResponseEntity<ApiResponse<?>> transferFloatBetweenBranches(
            @Valid @RequestBody MoneyMovementDtos.BranchTransferRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Branch float transfer posted successfully",
                moneyMovementService.transferFloatBetweenBranches(request)
        ));
    }

    @PostMapping("/currency-exchange")
    public ResponseEntity<ApiResponse<?>> exchangeCurrency(
            @Valid @RequestBody MoneyMovementDtos.CurrencyExchangeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Currency exchange posted successfully",
                moneyMovementService.exchangeCurrency(request)
        ));
    }

    @PostMapping("/expense/from-float")
    public ResponseEntity<ApiResponse<?>> payExpenseFromFloat(
            @Valid @RequestBody MoneyMovementDtos.ExpensePaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Expense paid from float successfully",
                moneyMovementService.payExpenseFromFloat(request)
        ));
    }

    @PostMapping("/expense/from-bank")
    public ResponseEntity<ApiResponse<?>> payExpenseFromBank(
            @Valid @RequestBody MoneyMovementDtos.ExpensePaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Expense paid from bank successfully",
                moneyMovementService.payExpenseFromBank(request)
        ));
    }

    @PostMapping("/float-adjustment")
    public ResponseEntity<ApiResponse<?>> adjustFloat(
            @Valid @RequestBody MoneyMovementDtos.FloatAdjustmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Float adjustment posted successfully",
                moneyMovementService.adjustFloat(request)
        ));
    }

    @PostMapping("/bank-adjustment")
    public ResponseEntity<ApiResponse<?>> adjustBank(
            @Valid @RequestBody MoneyMovementDtos.BankAdjustmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Bank adjustment posted successfully",
                moneyMovementService.adjustBank(request)
        ));
    }

    @GetMapping("/float/branch/{branchId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getFloatTransactionsByBranchAndCurrency(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Float transactions fetched successfully",
                moneyMovementService.getFloatTransactionsByBranchAndCurrency(branchId, currency)
        ));
    }

    @GetMapping("/float/branch/{branchId}/currency/{currency}/date/{transactionDate}")
    public ResponseEntity<ApiResponse<?>> getFloatTransactionsByBranchCurrencyAndDate(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDate
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Float transactions by date fetched successfully",
                moneyMovementService.getFloatTransactionsByBranchCurrencyAndDate(branchId, currency, transactionDate)
        ));
    }

    @GetMapping("/bank/branch/{branchId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getBankTransactionsByBranchAndCurrency(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank transactions fetched successfully",
                moneyMovementService.getBankTransactionsByBranchAndCurrency(branchId, currency)
        ));
    }

    @GetMapping("/float/loans-received/branch/{branchId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getLoanReceivedFloatMovements(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Loan received float movements fetched successfully",
                moneyMovementService.getLoanReceivedFloatMovements(branchId, currency)
        ));
    }

    @GetMapping("/float/loan-repayments/branch/{branchId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getLoanRepaymentFloatMovements(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Loan repayment float movements fetched successfully",
                moneyMovementService.getLoanRepaymentFloatMovements(branchId, currency)
        ));
    }

    @GetMapping("/float/loans-received/branch/{branchId}/currency/{currency}/date/{transactionDate}")
    public ResponseEntity<ApiResponse<?>> getLoanReceivedFloatMovementsByDate(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDate
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Loan received float movements by date fetched successfully",
                moneyMovementService.getLoanReceivedFloatMovementsByDate(branchId, currency, transactionDate)
        ));
    }

    @GetMapping("/float/loan-repayments/branch/{branchId}/currency/{currency}/date/{transactionDate}")
    public ResponseEntity<ApiResponse<?>> getLoanRepaymentFloatMovementsByDate(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDate
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Loan repayment float movements by date fetched successfully",
                moneyMovementService.getLoanRepaymentFloatMovementsByDate(branchId, currency, transactionDate)
        ));
    }

    @GetMapping("/bank/account/{bankAccountId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getBankTransactionsByAccountAndCurrency(
            @PathVariable Long bankAccountId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Bank account transactions fetched successfully",
                moneyMovementService.getBankTransactionsByAccountAndCurrency(bankAccountId, currency)
        ));
    }
}