package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.BankAccountDtos;
import com.basiltech.sipafin.model.BankAccount;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(target = "bankId", source = "bank.id")
    @Mapping(target = "bankName", source = "bank.name")
    @Mapping(target = "bankCode", source = "bank.code")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    BankAccountDtos.BankAccountResponse toResponse(BankAccount bankAccount);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BankAccountDtos.UpdateBankAccountRequest request, @MappingTarget BankAccount bankAccount);
}
