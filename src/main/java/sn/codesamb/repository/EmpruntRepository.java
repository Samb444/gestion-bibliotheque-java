package sn.codesamb.repository;

import sn.codesamb.model.Emprunt;

import java.util.List;
import java.util.Objects;

/**
 * Repository en mémoire pour la gestion des entités {@link Emprunt}.
 */
public class EmpruntRepository extends InMemoryRepository<Emprunt> {

    /**
     * Initialise le repository des emprunts en utilisant {@link Emprunt#getId()} comme extracteur d'identifiant.
     */
    public EmpruntRepository() {
        super(Emprunt::getId);
    }

    /**
     * Génère un nouvel identifiant unique pour un emprunt.
     *
     * @return le prochain identifiant disponible
     */
    public Long nextId() {
        return storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
    }

    /**
     * Recherche tous les emprunts d'un membre.
     *
     * @param membreId l'identifiant du membre
     * @return la liste des emprunts associés au membre
     */
    public List<Emprunt> findByMembreId(Long membreId) {
        if (membreId == null) {
            return List.of();
        }
        return findAll().stream()
                .filter(e -> Objects.equals(e.getMembre().getId(), membreId))
                .toList();
    }

    /**
     * Recherche tous les emprunts en cours d'un membre.
     *
     * @param membreId l'identifiant du membre
     * @return la liste des emprunts actuellement actifs du membre
     */
    public List<Emprunt> findEnCoursByMembreId(Long membreId) {
        if (membreId == null) {
            return List.of();
        }
        return findAll().stream()
                .filter(e -> Objects.equals(e.getMembre().getId(), membreId) && e.estEnCours())
                .toList();
    }

    /**
     * Recherche tous les emprunts en cours qui sont en retard.
     *
     * @return la liste des emprunts en retard
     */
    public List<Emprunt> findEnRetard() {
        return findAll().stream()
                .filter(e -> e.estEnCours() && e.estEnRetard())
                .toList();
    }
}
