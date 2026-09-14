package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.LoanDtos;
import com.basiltech.sipafin.model.LoanAccount;
import com.basiltech.sipafin.model.LoanTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "investorId", source = "investor.id")
    @Mapping(target = "investorName", source = "investor.fullName")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    LoanDtos.LoanAccountResponse toLoanAccountResponse(LoanAccount loanAccount);

    @Mapping(target = "loanAccountId", source = "loanAccount.id")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    LoanDtos.LoanTransactionResponse toLoanTransactionResponse(LoanTransaction loanTransaction);
}
