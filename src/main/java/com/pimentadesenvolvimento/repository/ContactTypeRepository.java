package com.pimentadesenvolvimento.repository;

import com.pimentadesenvolvimento.domain.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {
    Optional<ContactType> findByNameIgnoreCase(String name);
}