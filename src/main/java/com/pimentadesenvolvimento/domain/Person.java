package com.pimentadesenvolvimento.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a real-world person, optionally linked to a system user.
 * A person must always be associated with a {@link User}, but the reverse
 * is not required (a user may exist without a corresponding person record).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "tb_person")
public class Person extends BaseEntity {

    @Column(nullable = false)
    private String name;

    /**
     * date of birth or any relevant date for the person
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Document> documents = new HashSet<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Contact> contacts = new HashSet<>();

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
}
