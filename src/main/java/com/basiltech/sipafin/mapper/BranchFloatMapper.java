package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.BranchFloatDtos;
import com.basiltech.sipafin.model.BranchFloatAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BranchFloatMapper {

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "branchCode", source = "branch.code")
    BranchFloatDtos.BranchFloatAccountResponse toResponse(BranchFloatAccount branchFloatAccount);
}