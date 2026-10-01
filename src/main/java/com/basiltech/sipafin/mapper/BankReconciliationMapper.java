package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.BankReconciliationDtos;
import com.basiltech.sipafin.model.BankReconciliation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankReconciliationMapper {

    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "bankName", source = "bankAccount.bank.name")
    @Mapping(target = "accountName", source = "bankAccount.accountName")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    BankReconciliationDtos.BankReconciliationResponse toResponse(BankReconciliation reconciliation);
}