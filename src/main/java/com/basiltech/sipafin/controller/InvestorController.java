package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.InvestorDtos;
import com.basiltech.sipafin.service.InvestorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investors")
@RequiredArgsConstructor
public class InvestorController {

    private final InvestorService investorService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvestorDtos.InvestorResponse>> createInvestor(
            @Valid @RequestBody InvestorDtos.CreateInvestorRequest request
    ) {
        InvestorDtos.InvestorResponse response = investorService.createInvestor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(201, "Investor created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvestorDtos.InvestorResponse>>> getAllInvestors() {
        return ResponseEntity.ok(ApiResponse.of(200, "Investors fetched successfully", investorService.getAllInvestors()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestorDtos.InvestorResponse>> getInvestorById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(200, "Investor fetched successfully", investorService.getInvestorById(id)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<InvestorDtos.InvestorResponse>>> getActiveInvestors() {
        return ResponseEntity.ok(ApiResponse.of(200, "Active investors fetched successfully", investorService.getActiveInvestors()));
    }

    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<InvestorDtos.InvestorResponse>>> getInactiveInvestors() {
        return ResponseEntity.ok(ApiResponse.of(200, "Inactive investors fetched successfully", investorService.getInactiveInvestors()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InvestorDtos.InvestorResponse>>> searchInvestors(@RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.of(200, "Investor search completed successfully", investorService.searchInvestors(keyword)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestorDtos.InvestorResponse>> updateInvestor(
            @PathVariable Long id,
            @Valid @RequestBody InvestorDtos.UpdateInvestorRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Investor updated successfully", investorService.updateInvestor(id, request)));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<InvestorDtos.InvestorResponse>> activateInvestor(
            @PathVariable Long id,
            @Valid @RequestBody InvestorDtos.InvestorStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Investor activated successfully", investorService.activateInvestor(id, request)));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<InvestorDtos.InvestorResponse>> deactivateInvestor(
            @PathVariable Long id,
            @Valid @RequestBody InvestorDtos.InvestorStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.of(200, "Investor deactivated successfully", investorService.deactivateInvestor(id, request)));
    }
}
