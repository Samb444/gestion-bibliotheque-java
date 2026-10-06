package sn.codesamb.model;

/**
 * Représente la catégorie thématique d'un livre.
 */
public enum CategorieLivre {
    ROMAN("Roman"),
    SCIENCE("Science"),
    HISTOIRE("Histoire"),
    INFORMATIQUE("Informatique"),
    DROIT("Droit");

    private final String libelle;

    CategorieLivre(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
