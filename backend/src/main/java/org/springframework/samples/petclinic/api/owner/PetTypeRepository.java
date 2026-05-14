package org.springframework.samples.petclinic.api.owner;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetTypeRepository extends JpaRepository<PetType, Integer> {
}
