package org.springframework.samples.petclinic.api.owner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Integer> {

    Page<Owner> findByLastNameStartingWithIgnoreCase(String lastName, Pageable pageable);
}
