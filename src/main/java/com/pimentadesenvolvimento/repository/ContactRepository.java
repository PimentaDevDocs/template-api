package com.pimentadesenvolvimento.repository;

import com.pimentadesenvolvimento.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByPersonId(Long personId);
}