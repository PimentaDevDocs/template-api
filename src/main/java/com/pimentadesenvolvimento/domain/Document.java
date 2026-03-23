package com.pimentadesenvolvimento.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * A generic document belonging to a person (e.g. passport, ID card, driver
 * license). There may be multiple documents per person.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "tb_document")
public class Document extends BaseEntity {

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String number;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    @ToString.Exclude
    private Person person;
}
