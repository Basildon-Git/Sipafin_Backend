package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.Investor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestorRepository extends JpaRepository<Investor, Long> {

    Optional<Investor> findByPhoneNumberIgnoreCase(String phoneNumber);

    boolean existsByPhoneNumberIgnoreCase(String phoneNumber);

    boolean existsByPhoneNumberIgnoreCaseAndIdNot(String phoneNumber, Long id);

    List<Investor> findByActive(boolean active);

    List<Investor> findByFullNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrNationalIdContainingIgnoreCase(
            String fullName,
            String phoneNumber,
            String nationalId
    );
}