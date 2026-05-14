package org.springframework.samples.petclinic.api.visit;

import org.springframework.samples.petclinic.api.owner.Owner;
import org.springframework.samples.petclinic.api.owner.OwnerRepository;
import org.springframework.samples.petclinic.api.owner.Pet;
import org.springframework.samples.petclinic.api.owner.Visit;
import org.springframework.samples.petclinic.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VisitService {

    private final OwnerRepository ownerRepository;

    public VisitService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Transactional
    public Visit create(Integer ownerId, Integer petId, VisitRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));

        Pet pet = owner.getPets().stream()
            .filter(p -> p.getId().equals(petId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado"));

        Visit visit = new Visit();
        visit.setDate(request.date());
        visit.setDescription(request.description());
        visit.setPetId(petId);
        pet.getVisits().add(visit);

        ownerRepository.save(owner);
        return visit;
    }
}
