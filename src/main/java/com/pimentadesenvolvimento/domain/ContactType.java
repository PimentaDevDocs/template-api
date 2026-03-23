package com.pimentadesenvolvimento.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "tb_contact_type")
public class ContactType extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "icon_url")
    private String iconUrl;
}