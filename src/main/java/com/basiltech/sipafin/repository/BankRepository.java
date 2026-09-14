package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findByCodeIgnoreCase(String code);

    Optional<Bank> findByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Bank> findByActive(boolean active);

    List<Bank> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name,
            String code
    );
}
