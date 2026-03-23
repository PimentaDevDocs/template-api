package com.pimentadesenvolvimento.conroledebolsaoback.service;

import com.pimentadesenvolvimento.conroledebolsaoback.config.Messages;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.Person;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.User;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.PersonDTO;
import com.pimentadesenvolvimento.conroledebolsaoback.exception.ResourceNotFoundException;
import com.pimentadesenvolvimento.conroledebolsaoback.mapper.PersonMapper;
import com.pimentadesenvolvimento.conroledebolsaoback.repository.PersonRepository;
import com.pimentadesenvolvimento.conroledebolsaoback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final PersonMapper personMapper;

    @Transactional
    public PersonDTO create(PersonDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.UserService.USER_NOT_FOUND_BY_ID, dto.userId())));
        Person entity = personMapper.toEntity(dto);
        entity.setUser(user);
        Person saved = personRepository.save(entity);
        return personMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public PersonDTO findById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.Person.PERSON_NOT_FOUND_BY_ID, id)));
        return personMapper.toDto(person);
    }

    @Transactional(readOnly = true)
    public PersonDTO findByUserId(Long userId) {
        Person person = personRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Person not found for User ID: " + userId));
        return personMapper.toDto(person);
    }

    @Transactional(readOnly = true)
    public Page<PersonDTO> findAll(Pageable pageable) {
        return personRepository.findAll(pageable)
                .map(personMapper::toDto);
    }

    @Transactional
    public PersonDTO update(Long id, PersonDTO dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.Person.PERSON_NOT_FOUND_BY_ID, id)));

        if (dto.name() != null) {
            person.setName(dto.name());
        }
        if (dto.birthDate() != null) {
            person.setBirthDate(dto.birthDate());
        }

        Person updated = personRepository.save(person);
        return personMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Messages.Person.PERSON_NOT_FOUND_BY_ID, id)));
        person.softDelete();
        personRepository.save(person);
    }
}
