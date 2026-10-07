package sn.codesamb.gestionsallesport.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.codesamb.gestionsallesport.entity.Inscription;

import java.util.List;

/**
 * Repository Spring Data JPA pour la gestion des inscriptions.
 */
@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    /**
     * Recherche la liste des inscriptions d'un adhérent par son identifiant technique.
     *
     * @param adherentId Identifiant de l'adhérent
     * @return Liste des inscriptions correspondantes
     */
    List<Inscription> findByAdherentId(Long adherentId);

    /**
     * Recherche la liste des inscriptions à un cours par son identifiant technique.
     *
     * @param coursId Identifiant du cours
     * @return Liste des inscriptions correspondantes
     */
    List<Inscription> findByCoursId(Long coursId);

    /**
     * Vérifie si un adhérent est déjà inscrit à un cours donné.
     *
     * @param adherentId Identifiant de l'adhérent
     * @param coursId    Identifiant du cours
     * @return {@code true} si l'inscription existe déjà, sinon {@code false}
     */
    boolean existsByAdherentIdAndCoursId(Long adherentId, Long coursId);
}
