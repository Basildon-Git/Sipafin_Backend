package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.InvestorDtos;

import java.util.List;

public interface InvestorService {

    InvestorDtos.InvestorResponse createInvestor(InvestorDtos.CreateInvestorRequest request);

    List<InvestorDtos.InvestorResponse> getAllInvestors();

    List<InvestorDtos.InvestorResponse> getActiveInvestors();

    List<InvestorDtos.InvestorResponse> getInactiveInvestors();

    List<InvestorDtos.InvestorResponse> searchInvestors(String keyword);

    InvestorDtos.InvestorResponse getInvestorById(Long id);

    InvestorDtos.InvestorResponse updateInvestor(Long id, InvestorDtos.UpdateInvestorRequest request);

    InvestorDtos.InvestorResponse activateInvestor(Long id, InvestorDtos.InvestorStatusRequest request);

    InvestorDtos.InvestorResponse deactivateInvestor(Long id, InvestorDtos.InvestorStatusRequest request);
}