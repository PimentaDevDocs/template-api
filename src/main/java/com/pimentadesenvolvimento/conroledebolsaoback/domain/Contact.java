package com.pimentadesenvolvimento.conroledebolsaoback.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "tb_contact")
public class Contact extends BaseEntity {

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contact_type_id", nullable = false)
    private ContactType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    @ToString.Exclude
    private Person person;
}