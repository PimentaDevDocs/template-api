package com.pimentadesenvolvimento.conroledebolsaoback.repository;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {
    Optional<ContactType> findByNameIgnoreCase(String name);
}