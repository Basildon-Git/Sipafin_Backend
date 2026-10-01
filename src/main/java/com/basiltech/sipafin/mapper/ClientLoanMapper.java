package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.ClientLoanDtos;
import com.basiltech.sipafin.model.ClientLoanAccount;
import com.basiltech.sipafin.model.ClientLoanTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientLoanMapper {

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "fundingBankAccountId", source = "fundingBankAccount.id")
    @Mapping(target = "fundingBankAccountName", source = "fundingBankAccount.accountName")
    ClientLoanDtos.ClientLoanAccountResponse toAccountResponse(ClientLoanAccount account);

    @Mapping(target = "clientLoanAccountId", source = "clientLoanAccount.id")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    ClientLoanDtos.ClientLoanTransactionResponse toTransactionResponse(ClientLoanTransaction transaction);
}