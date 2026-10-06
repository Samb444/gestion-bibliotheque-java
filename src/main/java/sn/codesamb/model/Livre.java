package sn.codesamb.model;

import java.util.Objects;

/**
 * Représente un livre au sein de la bibliothèque.
 */
public class Livre {

    private Long id;
    private final String titre;
    private final String auteur;
    private final CategorieLivre categorie;
    private EtatLivre etat;

    /**
     * Constructeur avec état par défaut (DISPONIBLE).
     */
    public Livre(Long id, String titre, String auteur, CategorieLivre categorie) {
        this(id, titre, auteur, categorie, EtatLivre.DISPONIBLE);
    }

    /**
     * Constructeur complet.
     */
    public Livre(Long id, String titre, String auteur, CategorieLivre categorie, EtatLivre etat) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre du livre est obligatoire.");
        }
        if (auteur == null || auteur.isBlank()) {
            throw new IllegalArgumentException("L'auteur du livre est obligatoire.");
        }
        this.id = id;
        this.titre = titre.trim();
        this.auteur = auteur.trim();
        this.categorie = Objects.requireNonNull(categorie, "La catégorie du livre est obligatoire.");
        this.etat = Objects.requireNonNull(etat, "L'état du livre est obligatoire.");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public CategorieLivre getCategorie() {
        return categorie;
    }

    public EtatLivre getEtat() {
        return etat;
    }

    public void setEtat(EtatLivre etat) {
        this.etat = Objects.requireNonNull(etat, "L'état du livre ne peut pas être null.");
    }

    /**
     * Vérifie si le livre est disponible pour un emprunt.
     *
     * @return true si le livre est disponible, false sinon.
     */
    public boolean estDisponible() {
        return this.etat == EtatLivre.DISPONIBLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Livre livre = (Livre) o;
        return id != null && Objects.equals(id, livre.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 31;
    }

    @Override
    public String toString() {
        return "Livre{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", auteur='" + auteur + '\'' +
                ", categorie=" + categorie +
                ", etat=" + etat +
                '}';
    }
}
