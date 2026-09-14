package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.BranchDtos;
import com.basiltech.sipafin.mapper.BranchMapper;
import com.basiltech.sipafin.model.Branch;
import com.basiltech.sipafin.repository.BranchRepository;
import com.basiltech.sipafin.service.BranchService;
import com.basiltech.sipafin.web.ConflictException;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImp implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    @Transactional
    public BranchDtos.BranchResponse createBranch(BranchDtos.CreateBranchRequest request) {
        validateCreateRequest(request);

        Branch branch = branchMapper.toEntity(request);
        branch.setCode(request.code().trim().toUpperCase());
        branch.setName(request.name().trim());
        branch.setLocation(request.location() == null ? null : request.location().trim());
        branch.setActive(true);
        branch.setActionedBy(request.actionedBy());

        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDtos.BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Branch::getId).reversed())
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDtos.BranchResponse> getActiveBranches() {
        return branchRepository.findByActive(true)
                .stream()
                .sorted(Comparator.comparing(Branch::getId).reversed())
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDtos.BranchResponse> getInactiveBranches() {
        return branchRepository.findByActive(false)
                .stream()
                .sorted(Comparator.comparing(Branch::getId).reversed())
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchDtos.BranchResponse> searchBranches(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllBranches();
        }

        return branchRepository
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrLocationContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword
                )
                .stream()
                .sorted(Comparator.comparing(Branch::getId).reversed())
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDtos.BranchResponse getBranchById(Long id) {
        return branchMapper.toResponse(findBranch(id));
    }

    @Override
    @Transactional
    public BranchDtos.BranchResponse updateBranch(Long id, BranchDtos.UpdateBranchRequest request) {
        Branch branch = findBranch(id);

        validateUpdateRequest(id, request);

        branchMapper.updateEntity(request, branch);
        branch.setCode(request.code().trim().toUpperCase());
        branch.setName(request.name().trim());
        branch.setLocation(request.location() == null ? null : request.location().trim());
        branch.setActionedBy(request.actionedBy());

        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchDtos.BranchResponse activateBranch(Long id, BranchDtos.BranchStatusRequest request) {
        Branch branch = findBranch(id);
        branch.setActive(true);
        branch.setActionedBy(request.actionedBy());
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchDtos.BranchResponse deactivateBranch(Long id, BranchDtos.BranchStatusRequest request) {
        Branch branch = findBranch(id);
        branch.setActive(false);
        branch.setActionedBy(request.actionedBy());
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found"));
    }

    private void validateCreateRequest(BranchDtos.CreateBranchRequest request) {
        if (branchRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("Branch code already exists");
        }

        if (branchRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Branch name already exists");
        }
    }

    private void validateUpdateRequest(Long id, BranchDtos.UpdateBranchRequest request) {
        if (branchRepository.existsByCodeIgnoreCaseAndIdNot(request.code().trim(), id)) {
            throw new ConflictException("Branch code already exists");
        }

        if (branchRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Branch name already exists");
        }
    }
}
