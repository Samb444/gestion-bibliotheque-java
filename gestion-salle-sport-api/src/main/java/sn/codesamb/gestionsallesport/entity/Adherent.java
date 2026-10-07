package sn.codesamb.gestionsallesport.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entité JPA représentant un adhérent inscrit à la salle de sport.
 * <p>
 * Table de correspondance en base relationnelle : {@code adherents}.
 */
@Entity
@Table(name = "adherents")
public class Adherent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    /**
     * Liste des inscriptions de l'adhérent aux différents cours.
     * Relation bidirectionnelle 'One-to-Many' pilotée par le champ {@code adherent} de l'entité {@link Inscription}.
     */
    @OneToMany(mappedBy = "adherent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscription> inscriptions = new ArrayList<>();

    /**
     * Constructeur sans argument obligatoire pour la spécification JPA.
     */
    public Adherent() {
    }

    /**
     * Constructeur d'initialisation avec les données d'identité de l'adhérent.
     *
     * @param nom           Nom de famille
     * @param prenom        Prénom
     * @param email         Adresse électronique unique
     * @param telephone     Numéro de téléphone
     * @param dateNaissance Date de naissance (API java.time)
     */
    public Adherent(String nom, String prenom, String email, String telephone, LocalDate dateNaissance) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
    }

    // =========================================================================
    // Méthodes de synchronisation de la relation bidirectionnelle
    // =========================================================================

    public void addInscription(Inscription inscription) {
        if (inscription != null) {
            this.inscriptions.add(inscription);
            inscription.setAdherent(this);
        }
    }

    public void removeInscription(Inscription inscription) {
        if (inscription != null) {
            this.inscriptions.remove(inscription);
            inscription.setAdherent(null);
        }
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public List<Inscription> getInscriptions() {
        return inscriptions;
    }

    public void setInscriptions(List<Inscription> inscriptions) {
        this.inscriptions = inscriptions != null ? inscriptions : new ArrayList<>();
    }

    // =========================================================================
    // Identité d'entité : equals, hashCode & toString
    // Règle d'or : Ne JAMAIS inclure la collection 'inscriptions' pour éviter
    // les récursions infinies (StackOverflowError) et le déclenchement de requêtes N+1.
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Adherent adherent = (Adherent) o;
        // Si l'id technique est assigné, l'égalité porte sur l'id, sinon sur l'email métier
        if (id != null && adherent.id != null) {
            return Objects.equals(id, adherent.id);
        }
        return Objects.equals(email, adherent.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : email);
    }

    @Override
    public String toString() {
        return "Adherent{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", dateNaissance=" + dateNaissance +
                '}';
    }
}
