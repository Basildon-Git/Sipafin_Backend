package com.basiltech.sipafin.mapper;

import com.basiltech.sipafin.dto.BranchDtos;
import com.basiltech.sipafin.model.Branch;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    Branch toEntity(BranchDtos.CreateBranchRequest request);

    BranchDtos.BranchResponse toResponse(Branch branch);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BranchDtos.UpdateBranchRequest request, @MappingTarget Branch branch);
}
