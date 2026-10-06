package sn.codesamb.repository;

import sn.codesamb.model.Livre;

/**
 * Repository en mémoire pour la gestion des entités {@link Livre}.
 */
public class LivreRepository extends InMemoryRepository<Livre> {

    /**
     * Initialise le repository des livres en utilisant {@link Livre#getId()} comme extracteur d'identifiant.
     */
    public LivreRepository() {
        super(Livre::getId);
    }
}
