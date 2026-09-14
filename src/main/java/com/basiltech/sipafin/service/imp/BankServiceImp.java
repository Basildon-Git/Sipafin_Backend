package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.BankDtos;
import com.basiltech.sipafin.mapper.BankMapper;
import com.basiltech.sipafin.model.Bank;
import com.basiltech.sipafin.repository.BankRepository;
import com.basiltech.sipafin.service.BankService;
import com.basiltech.sipafin.web.ConflictException;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankServiceImp implements BankService {

    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    @Override
    @Transactional
    public BankDtos.BankResponse createBank(BankDtos.CreateBankRequest request) {
        validateCreateRequest(request);

        Bank bank = bankMapper.toEntity(request);
        bank.setName(request.name().trim());
        bank.setCode(request.code().trim().toUpperCase());
        bank.setActive(true);
        bank.setActionedBy(request.actionedBy());

        return bankMapper.toResponse(bankRepository.save(bank));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankDtos.BankResponse> getAllBanks() {
        return bankRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Bank::getId).reversed())
                .map(bankMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankDtos.BankResponse> getActiveBanks() {
        return bankRepository.findByActive(true)
                .stream()
                .sorted(Comparator.comparing(Bank::getId).reversed())
                .map(bankMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankDtos.BankResponse> getInactiveBanks() {
        return bankRepository.findByActive(false)
                .stream()
                .sorted(Comparator.comparing(Bank::getId).reversed())
                .map(bankMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankDtos.BankResponse> searchBanks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllBanks();
        }

        return bankRepository
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword, keyword)
                .stream()
                .sorted(Comparator.comparing(Bank::getId).reversed())
                .map(bankMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BankDtos.BankResponse getBankById(Long id) {
        return bankMapper.toResponse(findBank(id));
    }

    @Override
    @Transactional
    public BankDtos.BankResponse updateBank(Long id, BankDtos.UpdateBankRequest request) {
        Bank bank = findBank(id);

        validateUpdateRequest(id, request);

        bankMapper.updateEntity(request, bank);
        bank.setName(request.name().trim());
        bank.setCode(request.code().trim().toUpperCase());
        bank.setActionedBy(request.actionedBy());

        return bankMapper.toResponse(bankRepository.save(bank));
    }

    @Override
    @Transactional
    public BankDtos.BankResponse activateBank(Long id, BankDtos.BankStatusRequest request) {
        Bank bank = findBank(id);
        bank.setActive(true);
        bank.setActionedBy(request.actionedBy());
        return bankMapper.toResponse(bankRepository.save(bank));
    }

    @Override
    @Transactional
    public BankDtos.BankResponse deactivateBank(Long id, BankDtos.BankStatusRequest request) {
        Bank bank = findBank(id);
        bank.setActive(false);
        bank.setActionedBy(request.actionedBy());
        return bankMapper.toResponse(bankRepository.save(bank));
    }

    private Bank findBank(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank not found"));
    }

    private void validateCreateRequest(BankDtos.CreateBankRequest request) {
        if (bankRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("Bank code already exists");
        }

        if (bankRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Bank name already exists");
        }
    }

    private void validateUpdateRequest(Long id, BankDtos.UpdateBankRequest request) {
        if (bankRepository.existsByCodeIgnoreCaseAndIdNot(request.code().trim(), id)) {
            throw new ConflictException("Bank code already exists");
        }

        if (bankRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Bank name already exists");
        }
    }
}