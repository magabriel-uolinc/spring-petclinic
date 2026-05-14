package org.springframework.samples.petclinic.api.owner;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.samples.petclinic.model.NamedEntity;

@Entity
@Table(name = "types")
public class PetType extends NamedEntity {
}
