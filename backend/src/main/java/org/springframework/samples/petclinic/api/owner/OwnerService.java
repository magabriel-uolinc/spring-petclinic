package org.springframework.samples.petclinic.api.owner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public Page<Owner> findByLastName(String lastName, int page, int size) {
        String filter = (lastName == null) ? "" : lastName;
        return ownerRepository.findByLastNameStartingWithIgnoreCase(filter, PageRequest.of(page, size));
    }

    public Owner findById(Integer id) {
        return ownerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));
    }

    @Transactional
    public Owner create(OwnerRequest request) {
        Owner owner = new Owner();
        applyRequest(owner, request);
        return ownerRepository.save(owner);
    }

    @Transactional
    public Owner update(Integer id, OwnerRequest request) {
        Owner owner = findById(id);
        applyRequest(owner, request);
        return ownerRepository.save(owner);
    }

    private void applyRequest(Owner owner, OwnerRequest request) {
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setAddress(request.address());
        owner.setCity(request.city());
        owner.setTelephone(request.telephone());
    }
}
