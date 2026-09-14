package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.BankDtos;
import com.basiltech.sipafin.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @PostMapping
    public ResponseEntity<ApiResponse<BankDtos.BankResponse>> createBank(
            @Valid @RequestBody BankDtos.CreateBankRequest request
    ) {
        BankDtos.BankResponse response = bankService.createBank(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Bank created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankDtos.BankResponse>>> getAllBanks() {
        List<BankDtos.BankResponse> response = bankService.getAllBanks();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Banks fetched successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BankDtos.BankResponse>>> getActiveBanks() {
        List<BankDtos.BankResponse> response = bankService.getActiveBanks();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Active banks fetched successfully", response));
    }

    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<BankDtos.BankResponse>>> getInactiveBanks() {
        List<BankDtos.BankResponse> response = bankService.getInactiveBanks();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Inactive banks fetched successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BankDtos.BankResponse>>> searchBanks(
            @RequestParam String keyword
    ) {
        List<BankDtos.BankResponse> response = bankService.searchBanks(keyword);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Banks search completed successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankDtos.BankResponse>> getBankById(@PathVariable Long id) {
        BankDtos.BankResponse response = bankService.getBankById(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bank fetched successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BankDtos.BankResponse>> updateBank(
            @PathVariable Long id,
            @Valid @RequestBody BankDtos.UpdateBankRequest request
    ) {
        BankDtos.BankResponse response = bankService.updateBank(id, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bank updated successfully", response));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<BankDtos.BankResponse>> activateBank(
            @PathVariable Long id,
            @Valid @RequestBody BankDtos.BankStatusRequest request
    ) {
        BankDtos.BankResponse response = bankService.activateBank(id, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bank activated successfully", response));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<BankDtos.BankResponse>> deactivateBank(
            @PathVariable Long id,
            @Valid @RequestBody BankDtos.BankStatusRequest request
    ) {
        BankDtos.BankResponse response = bankService.deactivateBank(id, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Bank deactivated successfully", response));
    }
}