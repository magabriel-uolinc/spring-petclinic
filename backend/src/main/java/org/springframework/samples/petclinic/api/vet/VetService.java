package org.springframework.samples.petclinic.api.vet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VetService {

    private final VetRepository vetRepository;

    public VetService(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    public Page<Vet> findAll(int page, int size) {
        return vetRepository.findAll(PageRequest.of(page, size));
    }
}
