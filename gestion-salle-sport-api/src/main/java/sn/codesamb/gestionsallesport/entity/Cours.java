package sn.codesamb.gestionsallesport.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entité JPA représentant une séance de cours dispensée dans la salle de sport.
 * <p>
 * Table de correspondance en base relationnelle : {@code cours}.
 */
@Entity
@Table(name = "cours")
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TypeCours type;

    @Column(name = "capacite", nullable = false)
    private Integer capacite;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "salle", nullable = false, length = 50)
    private String salle;

    /**
     * Liste des inscriptions associées à cette séance de cours.
     * Relation bidirectionnelle 'One-to-Many' pilotée par le champ {@code cours} de l'entité {@link Inscription}.
     */
    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscription> inscriptions = new ArrayList<>();

    /**
     * Constructeur sans argument obligatoire pour la spécification JPA.
     */
    public Cours() {
    }

    /**
     * Constructeur d'initialisation complet d'une séance de cours.
     *
     * @param nom       Intitulé du cours
     * @param type      Type de discipline sportive (enum TypeCours)
     * @param capacite  Capacité maximale d'accueil (entier strictement positif)
     * @param dateHeure Date et heure de début de la séance (API java.time)
     * @param salle     Nom ou numéro de la salle
     */
    public Cours(String nom, TypeCours type, Integer capacite, LocalDateTime dateHeure, String salle) {
        setCapacite(capacite);
        this.nom = nom;
        this.type = type;
        this.dateHeure = dateHeure;
        this.salle = salle;
    }

    // =========================================================================
    // Méthodes de synchronisation de la relation bidirectionnelle
    // =========================================================================

    public void addInscription(Inscription inscription) {
        if (inscription != null) {
            this.inscriptions.add(inscription);
            inscription.setCours(this);
        }
    }

    public void removeInscription(Inscription inscription) {
        if (inscription != null) {
            this.inscriptions.remove(inscription);
            inscription.setCours(null);
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

    public TypeCours getType() {
        return type;
    }

    public void setType(TypeCours type) {
        this.type = type;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        if (capacite != null && capacite <= 0) {
            throw new IllegalArgumentException("La capacité du cours doit être un nombre entier strictement positif.");
        }
        this.capacite = capacite;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public List<Inscription> getInscriptions() {
        return inscriptions;
    }

    public void setInscriptions(List<Inscription> inscriptions) {
        this.inscriptions = inscriptions != null ? inscriptions : new ArrayList<>();
    }

    // =========================================================================
    // Identité d'entité : equals, hashCode & toString
    // Exclusion formelle de la collection 'inscriptions' pour éviter toute récursion.
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cours cours = (Cours) o;
        if (id != null && cours.id != null) {
            return Objects.equals(id, cours.id);
        }
        return Objects.equals(nom, cours.nom) &&
                type == cours.type &&
                Objects.equals(dateHeure, cours.dateHeure) &&
                Objects.equals(salle, cours.salle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : Objects.hash(nom, type, dateHeure, salle));
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type=" + type +
                ", capacite=" + capacite +
                ", dateHeure=" + dateHeure +
                ", salle='" + salle + '\'' +
                '}';
    }
}
