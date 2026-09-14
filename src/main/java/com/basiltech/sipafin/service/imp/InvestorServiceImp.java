package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.InvestorDtos;
import com.basiltech.sipafin.mapper.InvestorMapper;
import com.basiltech.sipafin.model.Investor;
import com.basiltech.sipafin.repository.InvestorRepository;
import com.basiltech.sipafin.service.InvestorService;
import com.basiltech.sipafin.web.ConflictException;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestorServiceImp implements InvestorService {

    private final InvestorRepository investorRepository;
    private final InvestorMapper investorMapper;

    @Override
    @Transactional
    public InvestorDtos.InvestorResponse createInvestor(InvestorDtos.CreateInvestorRequest request) {
        if (investorRepository.existsByPhoneNumberIgnoreCase(request.phoneNumber().trim())) {
            throw new ConflictException("Investor phone number already exists");
        }

        Investor investor = investorMapper.toEntity(request);
        investor.setFullName(request.fullName().trim());
        investor.setPhoneNumber(request.phoneNumber().trim());
        investor.setNationalId(request.nationalId() == null ? null : request.nationalId().trim());
        investor.setAddress(request.address() == null ? null : request.address().trim());
        investor.setActive(true);
        investor.setActionedBy(request.actionedBy());

        return investorMapper.toResponse(investorRepository.save(investor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestorDtos.InvestorResponse> getAllInvestors() {
        return investorRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Investor::getId).reversed())
                .map(investorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestorDtos.InvestorResponse> getActiveInvestors() {
        return investorRepository.findByActive(true)
                .stream()
                .sorted(Comparator.comparing(Investor::getId).reversed())
                .map(investorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestorDtos.InvestorResponse> getInactiveInvestors() {
        return investorRepository.findByActive(false)
                .stream()
                .sorted(Comparator.comparing(Investor::getId).reversed())
                .map(investorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestorDtos.InvestorResponse> searchInvestors(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllInvestors();
        }

        return investorRepository
                .findByFullNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrNationalIdContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword
                )
                .stream()
                .sorted(Comparator.comparing(Investor::getId).reversed())
                .map(investorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvestorDtos.InvestorResponse getInvestorById(Long id) {
        return investorMapper.toResponse(findInvestor(id));
    }

    @Override
    @Transactional
    public InvestorDtos.InvestorResponse updateInvestor(Long id, InvestorDtos.UpdateInvestorRequest request) {
        Investor investor = findInvestor(id);

        if (investorRepository.existsByPhoneNumberIgnoreCaseAndIdNot(request.phoneNumber().trim(), id)) {
            throw new ConflictException("Investor phone number already exists");
        }

        investor.setFullName(request.fullName().trim());
        investor.setPhoneNumber(request.phoneNumber().trim());
        investor.setNationalId(request.nationalId() == null ? null : request.nationalId().trim());
        investor.setAddress(request.address() == null ? null : request.address().trim());
        investor.setActive(request.active());
        investor.setActionedBy(request.actionedBy());

        return investorMapper.toResponse(investorRepository.save(investor));
    }

    @Override
    @Transactional
    public InvestorDtos.InvestorResponse activateInvestor(Long id, InvestorDtos.InvestorStatusRequest request) {
        Investor investor = findInvestor(id);
        investor.setActive(true);
        investor.setActionedBy(request.actionedBy());
        return investorMapper.toResponse(investorRepository.save(investor));
    }

    @Override
    @Transactional
    public InvestorDtos.InvestorResponse deactivateInvestor(Long id, InvestorDtos.InvestorStatusRequest request) {
        Investor investor = findInvestor(id);
        investor.setActive(false);
        investor.setActionedBy(request.actionedBy());
        return investorMapper.toResponse(investorRepository.save(investor));
    }

    private Investor findInvestor(Long id) {
        return investorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Investor not found"));
    }
}