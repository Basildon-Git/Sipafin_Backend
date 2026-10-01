package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.CommissionDtos;
import com.basiltech.sipafin.model.BankCommissionAccount;
import com.basiltech.sipafin.model.Commission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommissionMapper {

    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "bankName", source = "bankAccount.bank.name")
    @Mapping(target = "bankCode", source = "bankAccount.bank.code")
    @Mapping(target = "accountName", source = "bankAccount.accountName")
    @Mapping(target = "accountNumber", source = "bankAccount.accountNumber")
    CommissionDtos.BankCommissionAccountResponse toCommissionAccountResponse(BankCommissionAccount account);

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "bankName", source = "bankAccount.bank.name")
    @Mapping(target = "accountName", source = "bankAccount.accountName")
    @Mapping(target = "commissionAccountId", source = "commissionAccount.id")
    CommissionDtos.CommissionResponse toCommissionResponse(Commission commission);
}