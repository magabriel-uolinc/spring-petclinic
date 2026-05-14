package org.springframework.samples.petclinic.api.vet;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.samples.petclinic.model.NamedEntity;

@Entity
@Table(name = "specialties")
public class Specialty extends NamedEntity {
}
