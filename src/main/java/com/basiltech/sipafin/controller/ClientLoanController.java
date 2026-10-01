package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.ClientLoanDtos;
import com.basiltech.sipafin.model.ClientLoanStatus;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.service.ClientLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/client-loans")
@RequiredArgsConstructor
public class ClientLoanController {

    private final ClientLoanService clientLoanService;

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<?>> issueClientLoan(
            @Valid @RequestBody ClientLoanDtos.IssueClientLoanRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(
                HttpStatus.CREATED.value(),
                "Client loan issued successfully",
                clientLoanService.issueClientLoan(request)
        ));
    }

    @PostMapping("/{clientLoanAccountId}/repay")
    public ResponseEntity<ApiResponse<?>> repayClientLoan(
            @PathVariable Long clientLoanAccountId,
            @Valid @RequestBody ClientLoanDtos.RepayClientLoanRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loan repayment posted successfully",
                clientLoanService.repayClientLoan(clientLoanAccountId, request)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllClientLoans() {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loans fetched successfully",
                clientLoanService.getAllClientLoans()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getClientLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loan fetched successfully",
                clientLoanService.getClientLoanById(id)
        ));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<?>> getClientLoansByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loans by branch fetched successfully",
                clientLoanService.getClientLoansByBranch(branchId)
        ));
    }

    @GetMapping("/branch/{branchId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getClientLoansByBranchAndCurrency(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loans by branch and currency fetched successfully",
                clientLoanService.getClientLoansByBranchAndCurrency(branchId, currency)
        ));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<?>> getClientLoansByStatus(@PathVariable ClientLoanStatus status) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loans by status fetched successfully",
                clientLoanService.getClientLoansByStatus(status)
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchClientLoans(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loan search completed successfully",
                clientLoanService.searchClientLoans(keyword)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateClientLoan(
            @PathVariable Long id,
            @Valid @RequestBody ClientLoanDtos.UpdateClientLoanRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loan updated successfully",
                clientLoanService.updateClientLoan(id, request)
        ));
    }

    @GetMapping("/{clientLoanAccountId}/transactions")
    public ResponseEntity<ApiResponse<?>> getClientLoanTransactions(@PathVariable Long clientLoanAccountId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Client loan transactions fetched successfully",
                clientLoanService.getClientLoanTransactions(clientLoanAccountId)
        ));
    }
}