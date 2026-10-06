package sn.codesamb.repository;

import sn.codesamb.model.Membre;

/**
 * Repository en mémoire pour la gestion des entités {@link Membre}.
 */
public class MembreRepository extends InMemoryRepository<Membre> {

    /**
     * Initialise le repository des membres en utilisant {@link Membre#getId()} comme extracteur d'identifiant.
     */
    public MembreRepository() {
        super(Membre::getId);
    }
}
