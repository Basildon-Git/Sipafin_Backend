package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.InvestorDtos;
import com.basiltech.sipafin.model.Investor;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InvestorMapper {

    Investor toEntity(InvestorDtos.CreateInvestorRequest request);

    InvestorDtos.InvestorResponse toResponse(Investor investor);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(InvestorDtos.UpdateInvestorRequest request, @MappingTarget Investor investor);
}