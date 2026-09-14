package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.LoanDtos;
import com.basiltech.sipafin.model.LoanStatus;
import com.basiltech.sipafin.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/receive")
    public ResponseEntity<ApiResponse<LoanDtos.LoanAccountResponse>> receiveLoan(
            @Valid @RequestBody LoanDtos.ReceiveLoanRequest request
    ) {
        LoanDtos.LoanAccountResponse response = loanService.receiveLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(201, "Loan received successfully", response));
    }

    @PostMapping("/{loanAccountId}/repay")
    public ResponseEntity<ApiResponse<LoanDtos.LoanAccountResponse>> repayLoan(
            @PathVariable Long loanAccountId,
            @Valid @RequestBody LoanDtos.RepayLoanRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loan repayment posted successfully", loanService.repayLoan(loanAccountId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanDtos.LoanAccountResponse>>> getAllLoans() {
        return ResponseEntity.ok(ApiResponse.of(200, "Loans fetched successfully", loanService.getAllLoans()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanDtos.LoanAccountResponse>> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loan fetched successfully", loanService.getLoanById(id)));
    }

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<ApiResponse<List<LoanDtos.LoanAccountResponse>>> getLoansByInvestor(@PathVariable Long investorId) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loans by investor fetched successfully", loanService.getLoansByInvestor(investorId)));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<LoanDtos.LoanAccountResponse>>> getLoansByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loans by branch fetched successfully", loanService.getLoansByBranch(branchId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<LoanDtos.LoanAccountResponse>>> getLoansByStatus(@PathVariable LoanStatus status) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loans by status fetched successfully", loanService.getLoansByStatus(status)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LoanDtos.LoanAccountResponse>>> searchLoansByInvestorName(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loan search completed successfully", loanService.searchLoansByInvestorName(keyword)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanDtos.LoanAccountResponse>> updateLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanDtos.UpdateLoanRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loan updated successfully", loanService.updateLoan(id, request)));
    }

    @GetMapping("/{loanAccountId}/transactions")
    public ResponseEntity<ApiResponse<List<LoanDtos.LoanTransactionResponse>>> getLoanTransactions(
            @PathVariable Long loanAccountId
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Loan transactions fetched successfully", loanService.getLoanTransactions(loanAccountId)));
    }
}
