package com.pimentadesenvolvimento.conroledebolsaoback.repository;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u")
    List<User> findAllWithRoles();

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleNome")
    List<User> findByRoleNome(@Param("roleNome") String roleNome);

    @Override
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"roles"})
    List<User> findAll();

    @Override
    @EntityGraph(attributePaths = {"roles"})
    Page<User> findAll(Pageable pageable);
}