package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.BankDtos;
import com.basiltech.sipafin.model.Bank;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BankMapper {

    Bank toEntity(BankDtos.CreateBankRequest request);

    BankDtos.BankResponse toResponse(Bank bank);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BankDtos.UpdateBankRequest request, @MappingTarget Bank bank);
}
