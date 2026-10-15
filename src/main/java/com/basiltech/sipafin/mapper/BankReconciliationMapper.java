package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.BankReconciliationDtos;
import com.basiltech.sipafin.model.BankReconciliation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankReconciliationMapper {

    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "bankName", source = "bankAccount.bank.name")
    @Mapping(target = "bankCode", source = "bankAccount.bank.code")
    @Mapping(target = "accountName", source = "bankAccount.accountName")
    @Mapping(target = "accountNumber", source = "bankAccount.accountNumber")
    @Mapping(target = "branchId", expression = "java(reconciliation.getBranch() == null ? null : reconciliation.getBranch().getId())")
    @Mapping(target = "branchName", expression = "java(reconciliation.getBranch() == null ? null : reconciliation.getBranch().getName())")
    @Mapping(target = "items", ignore = true)
    BankReconciliationDtos.BankReconciliationResponse toResponse(BankReconciliation reconciliation);
}