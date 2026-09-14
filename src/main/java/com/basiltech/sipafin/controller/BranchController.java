package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.ApiResponse;
import com.basiltech.sipafin.dto.BranchDtos;
import com.basiltech.sipafin.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<BranchDtos.BranchResponse>> createBranch(
            @Valid @RequestBody BranchDtos.CreateBranchRequest request
    ) {
        BranchDtos.BranchResponse response = branchService.createBranch(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "Branch created successfully", response));
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchDtos.BranchResponse>>> getAllBranches() {
        List<BranchDtos.BranchResponse> response = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Branches fetched successfully", response));
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BranchDtos.BranchResponse>>> getActiveBranches() {
        List<BranchDtos.BranchResponse> response = branchService.getActiveBranches();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Active branches fetched successfully", response));
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<BranchDtos.BranchResponse>>> getInactiveBranches() {
        List<BranchDtos.BranchResponse> response = branchService.getInactiveBranches();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Inactive branches fetched successfully", response));
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BranchDtos.BranchResponse>>> searchBranches(
            @RequestParam String keyword
    ) {
        List<BranchDtos.BranchResponse> response = branchService.searchBranches(keyword);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Branches search completed successfully", response));
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'ACCOUNTANT', 'AUDITOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDtos.BranchResponse>> getBranchById(@PathVariable Long id) {
        BranchDtos.BranchResponse response = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Branch fetched successfully", response));
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDtos.BranchResponse>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchDtos.UpdateBranchRequest request
    ) {
        BranchDtos.BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Branch updated successfully", response));
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<BranchDtos.BranchResponse>> activateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchDtos.BranchStatusRequest request
    ) {
        BranchDtos.BranchResponse response = branchService.activateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Branch activated successfully", response));
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<BranchDtos.BranchResponse>> deactivateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchDtos.BranchStatusRequest request
    ) {
        BranchDtos.BranchResponse response = branchService.deactivateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "Branch deactivated successfully", response));
    }
}