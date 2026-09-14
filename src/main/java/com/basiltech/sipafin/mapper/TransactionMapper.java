package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.TransactionDtos;
import com.basiltech.sipafin.model.BankTransaction;
import com.basiltech.sipafin.model.PettyCashTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    TransactionDtos.CashTransactionResponse toCashResponse(PettyCashTransaction transaction);

    @Mapping(target = "bankAccountId", source = "bankAccount.id")
    @Mapping(target = "accountName", source = "bankAccount.accountName")
    @Mapping(target = "accountNumber", source = "bankAccount.accountNumber")
    @Mapping(target = "bankId", source = "bankAccount.bank.id")
    @Mapping(target = "bankName", source = "bankAccount.bank.name")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    TransactionDtos.BankTransactionResponse toBankResponse(BankTransaction transaction);
}
