package sn.codesamb.model;

/**
 * Représente l'état de disponibilité d'un livre dans la bibliothèque.
 */
public enum EtatLivre {
    DISPONIBLE("Disponible"),
    EMPRUNTE("Emprunté");

    private final String libelle;

    EtatLivre(String libelle) {
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
