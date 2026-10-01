package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.BranchFloatDtos;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.service.BranchFloatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branch-floats")
@RequiredArgsConstructor
public class BranchFloatController {

    private final BranchFloatService branchFloatService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createFloatAccount(
            @Valid @RequestBody BranchFloatDtos.CreateBranchFloatAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED.value(),
                        "Branch float account created successfully",
                        branchFloatService.createFloatAccount(request)
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllFloatAccounts() {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch float accounts fetched successfully",
                branchFloatService.getAllFloatAccounts()
        ));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<?>> getFloatAccountsByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch float accounts fetched successfully",
                branchFloatService.getFloatAccountsByBranch(branchId)
        ));
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getFloatAccountsByCurrency(@PathVariable CurrencyCode currency) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Float accounts by currency fetched successfully",
                branchFloatService.getFloatAccountsByCurrency(currency)
        ));
    }

    @GetMapping("/branch/{branchId}/currency/{currency}")
    public ResponseEntity<ApiResponse<?>> getFloatAccountByBranchAndCurrency(
            @PathVariable Long branchId,
            @PathVariable CurrencyCode currency
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch float account fetched successfully",
                branchFloatService.getFloatAccountByBranchAndCurrency(branchId, currency)
        ));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<?>> activateFloatAccount(
            @PathVariable Long id,
            @Valid @RequestBody BranchFloatDtos.BranchFloatStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch float account activated successfully",
                branchFloatService.activateFloatAccount(id, request)
        ));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<?>> deactivateFloatAccount(
            @PathVariable Long id,
            @Valid @RequestBody BranchFloatDtos.BranchFloatStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                HttpStatus.OK.value(),
                "Branch float account deactivated successfully",
                branchFloatService.deactivateFloatAccount(id, request)
        ));
    }
}