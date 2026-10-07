package sn.codesamb.gestionsallesport.service;

import org.springframework.stereotype.Service;
import sn.codesamb.gestionsallesport.entity.Inscription;
import sn.codesamb.gestionsallesport.repository.InscriptionRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations sur les inscriptions aux cours.
 */
@Service
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;

    public InscriptionService(InscriptionRepository inscriptionRepository) {
        this.inscriptionRepository = inscriptionRepository;
    }

    public List<Inscription> findAll() {
        return inscriptionRepository.findAll();
    }

    public Optional<Inscription> findById(Long id) {
        return inscriptionRepository.findById(id);
    }

    public List<Inscription> findByAdherentId(Long adherentId) {
        return inscriptionRepository.findByAdherentId(adherentId);
    }

    public List<Inscription> findByCoursId(Long coursId) {
        return inscriptionRepository.findByCoursId(coursId);
    }

    public boolean existsByAdherentIdAndCoursId(Long adherentId, Long coursId) {
        return inscriptionRepository.existsByAdherentIdAndCoursId(adherentId, coursId);
    }

    public Inscription save(Inscription inscription) {
        return inscriptionRepository.save(inscription);
    }

    public void deleteById(Long id) {
        inscriptionRepository.deleteById(id);
    }
}
