package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.MoneyMovementDtos;
import com.basiltech.sipafin.model.BankTransaction;
import com.basiltech.sipafin.model.BranchFloatTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MoneyMovementMapper {

    @Mapping(target = "branchFloatAccountId", source = "branchFloatAccount.id")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "branchCode", source = "branch.code")
    MoneyMovementDtos.BranchFloatTransactionResponse toFloatResponse(BranchFloatTransaction transaction);

    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "accountName", source = "bankAccount.accountName")
    @Mapping(target = "accountNumber", source = "bankAccount.accountNumber")
    @Mapping(target = "bankId", source = "bankAccount.bank.id")
    @Mapping(target = "bankName", source = "bankAccount.bank.name")
    @Mapping(target = "bankCode", source = "bankAccount.bank.code")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "branchCode", source = "branch.code")
    MoneyMovementDtos.BankTransactionResponse toBankResponse(BankTransaction transaction);
}