package org.springframework.samples.petclinic.api.vet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface VetRepository extends Repository<Vet, Integer> {

    Page<Vet> findAll(Pageable pageable);
}
