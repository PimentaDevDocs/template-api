package com.pimentadesenvolvimento.conroledebolsaoback.repository;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String nome);
}