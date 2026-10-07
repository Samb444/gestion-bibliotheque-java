package sn.codesamb.gestionsallesport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.codesamb.gestionsallesport.entity.Adherent;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour la gestion des adhérents.
 */
@Repository
public interface AdherentRepository extends JpaRepository<Adherent, Long> {

    /**
     * Recherche un adhérent par son adresse email unique.
     *
     * @param email Adresse email recherchée
     * @return un {@link Optional} contenant l'adhérent s'il existe
     */
    Optional<Adherent> findByEmail(String email);
}
