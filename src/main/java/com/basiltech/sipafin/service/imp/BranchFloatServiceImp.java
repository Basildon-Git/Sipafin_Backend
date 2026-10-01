package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.BranchFloatDtos;
import com.basiltech.sipafin.mapper.BranchFloatMapper;
import com.basiltech.sipafin.model.Branch;
import com.basiltech.sipafin.model.BranchFloatAccount;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.repository.BranchFloatAccountRepository;
import com.basiltech.sipafin.repository.BranchRepository;
import com.basiltech.sipafin.service.BranchFloatService;
import com.basiltech.sipafin.web.ConflictException;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchFloatServiceImp implements BranchFloatService {

    private final BranchRepository branchRepository;
    private final BranchFloatAccountRepository branchFloatAccountRepository;
    private final BranchFloatMapper branchFloatMapper;

    @Override
    @Transactional
    public BranchFloatDtos.BranchFloatAccountResponse createFloatAccount(
            BranchFloatDtos.CreateBranchFloatAccountRequest request
    ) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        if (branchFloatAccountRepository.existsByBranchIdAndCurrency(request.branchId(), request.currency())) {
            throw new ConflictException("Float account already exists for this branch and currency");
        }

        BranchFloatAccount floatAccount = new BranchFloatAccount();
        floatAccount.setBranch(branch);
        floatAccount.setCurrency(request.currency());
        floatAccount.setCurrentBalance(request.openingBalance());
        floatAccount.setActive(true);
        floatAccount.setActionedBy(request.actionedBy());

        return branchFloatMapper.toResponse(branchFloatAccountRepository.save(floatAccount));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchFloatDtos.BranchFloatAccountResponse> getAllFloatAccounts() {
        return branchFloatAccountRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BranchFloatAccount::getId).reversed())
                .map(branchFloatMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchFloatDtos.BranchFloatAccountResponse> getFloatAccountsByBranch(Long branchId) {
        return branchFloatAccountRepository.findByBranchId(branchId)
                .stream()
                .sorted(Comparator.comparing(BranchFloatAccount::getId).reversed())
                .map(branchFloatMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchFloatDtos.BranchFloatAccountResponse> getFloatAccountsByCurrency(CurrencyCode currency) {
        return branchFloatAccountRepository.findByCurrency(currency)
                .stream()
                .sorted(Comparator.comparing(BranchFloatAccount::getId).reversed())
                .map(branchFloatMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchFloatDtos.BranchFloatAccountResponse getFloatAccountByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    ) {
        BranchFloatAccount floatAccount = branchFloatAccountRepository.findByBranchIdAndCurrency(branchId, currency)
                .orElseThrow(() -> new NotFoundException("Float account not found for branch and currency"));

        return branchFloatMapper.toResponse(floatAccount);
    }

    @Override
    @Transactional
    public BranchFloatDtos.BranchFloatAccountResponse activateFloatAccount(
            Long id,
            BranchFloatDtos.BranchFloatStatusRequest request
    ) {
        BranchFloatAccount floatAccount = findFloatAccount(id);
        floatAccount.setActive(true);
        floatAccount.setActionedBy(request.actionedBy());
        return branchFloatMapper.toResponse(branchFloatAccountRepository.save(floatAccount));
    }

    @Override
    @Transactional
    public BranchFloatDtos.BranchFloatAccountResponse deactivateFloatAccount(
            Long id,
            BranchFloatDtos.BranchFloatStatusRequest request
    ) {
        BranchFloatAccount floatAccount = findFloatAccount(id);
        floatAccount.setActive(false);
        floatAccount.setActionedBy(request.actionedBy());
        return branchFloatMapper.toResponse(branchFloatAccountRepository.save(floatAccount));
    }

    private BranchFloatAccount findFloatAccount(Long id) {
        return branchFloatAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Float account not found"));
    }
}