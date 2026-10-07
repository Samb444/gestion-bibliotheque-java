package sn.codesamb.gestionsallesport.service;

import org.springframework.stereotype.Service;
import sn.codesamb.gestionsallesport.entity.Cours;
import sn.codesamb.gestionsallesport.repository.CoursRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations sur les séances de cours.
 */
@Service
public class CoursService {

    private final CoursRepository coursRepository;

    public CoursService(CoursRepository coursRepository) {
        this.coursRepository = coursRepository;
    }

    public List<Cours> findAll() {
        return coursRepository.findAll();
    }

    public Optional<Cours> findById(Long id) {
        return coursRepository.findById(id);
    }

    public Cours save(Cours cours) {
        return coursRepository.save(cours);
    }

    public void deleteById(Long id) {
        coursRepository.deleteById(id);
    }
}
