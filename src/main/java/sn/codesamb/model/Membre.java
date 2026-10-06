package sn.codesamb.model;

import java.util.Objects;

/**
 * Représente un membre adhérent de la bibliothèque.
 */
public class Membre {

    private Long id;
    private final String nom;
    private final String prenom;
    private String email;

    public Membre(Long id, String nom, String prenom, String email) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom du membre est obligatoire.");
        }
        if (prenom == null || prenom.isBlank()) {
            throw new IllegalArgumentException("Le prénom du membre est obligatoire.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email du membre est obligatoire.");
        }
        this.id = id;
        this.nom = nom.trim();
        this.prenom = prenom.trim();
        this.email = email.trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email du membre ne peut pas être vide.");
        }
        this.email = email.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Membre membre = (Membre) o;
        return id != null && Objects.equals(id, membre.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 31;
    }

    @Override
    public String toString() {
        return "Membre{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
