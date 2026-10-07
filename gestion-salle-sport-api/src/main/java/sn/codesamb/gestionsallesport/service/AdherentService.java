package sn.codesamb.gestionsallesport.service;

import org.springframework.stereotype.Service;
import sn.codesamb.gestionsallesport.entity.Adherent;
import sn.codesamb.gestionsallesport.repository.AdherentRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations sur les adhérents.
 */
@Service
public class AdherentService {

    private final AdherentRepository adherentRepository;

    public AdherentService(AdherentRepository adherentRepository) {
        this.adherentRepository = adherentRepository;
    }

    public List<Adherent> findAll() {
        return adherentRepository.findAll();
    }

    public Optional<Adherent> findById(Long id) {
        return adherentRepository.findById(id);
    }

    public Optional<Adherent> findByEmail(String email) {
        return adherentRepository.findByEmail(email);
    }

    public Adherent save(Adherent adherent) {
        return adherentRepository.save(adherent);
    }

    public void deleteById(Long id) {
        adherentRepository.deleteById(id);
    }
}
