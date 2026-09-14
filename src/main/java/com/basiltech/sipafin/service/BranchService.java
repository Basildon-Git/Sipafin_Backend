package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.BranchDtos;

import java.util.List;

public interface BranchService {

    BranchDtos.BranchResponse createBranch(BranchDtos.CreateBranchRequest request);

    List<BranchDtos.BranchResponse> getAllBranches();

    List<BranchDtos.BranchResponse> getActiveBranches();

    List<BranchDtos.BranchResponse> getInactiveBranches();

    List<BranchDtos.BranchResponse> searchBranches(String keyword);

    BranchDtos.BranchResponse getBranchById(Long id);

    BranchDtos.BranchResponse updateBranch(Long id, BranchDtos.UpdateBranchRequest request);

    BranchDtos.BranchResponse activateBranch(Long id, BranchDtos.BranchStatusRequest request);

    BranchDtos.BranchResponse deactivateBranch(Long id, BranchDtos.BranchStatusRequest request);
}