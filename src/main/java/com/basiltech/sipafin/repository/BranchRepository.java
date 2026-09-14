package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByCodeIgnoreCase(String code);

    Optional<Branch> findByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Branch> findByActive(boolean active);

    List<Branch> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrLocationContainingIgnoreCase(
            String name,
            String code,
            String location
    );
}