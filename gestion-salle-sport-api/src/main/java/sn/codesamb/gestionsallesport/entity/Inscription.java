package sn.codesamb.gestionsallesport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité JPA représentant l'inscription d'un adhérent à un cours spécifique.
 * <p>
 * Table de correspondance en base relationnelle : {@code inscriptions}.
 * Modélise la table de jointure avec attributs complémentaires (date d'inscription et présence).
 */
@Entity
@Table(name = "inscriptions")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "presence", nullable = false, length = 20)
    private StatutPresence presence = StatutPresence.ABSENT;

    /**
     * Adhérent concerné par l'inscription.
     * Relation Many-to-One obligatoire.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;

    /**
     * Séance de cours concernée par l'inscription.
     * Relation Many-to-One obligatoire.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    /**
     * Constructeur sans argument obligatoire pour la spécification JPA.
     */
    public Inscription() {
        this.dateInscription = LocalDateTime.now();
    }

    /**
     * Constructeur d'initialisation avec l'adhérent et le cours cibles.
     *
     * @param adherent Adhérent qui s'inscrit
     * @param cours    Cours choisi
     */
    public Inscription(Adherent adherent, Cours cours) {
        this(adherent, cours, LocalDateTime.now(), StatutPresence.ABSENT);
    }

    /**
     * Constructeur complet d'inscription.
     *
     * @param adherent        Adhérent qui s'inscrit
     * @param cours           Cours choisi
     * @param dateInscription Date et heure d'enregistrement de l'inscription
     * @param presence        Statut initial de présence
     */
    public Inscription(Adherent adherent, Cours cours, LocalDateTime dateInscription, StatutPresence presence) {
        this.adherent = adherent;
        this.cours = cours;
        this.dateInscription = dateInscription != null ? dateInscription : LocalDateTime.now();
        this.presence = presence != null ? presence : StatutPresence.ABSENT;
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

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public StatutPresence getPresence() {
        return presence;
    }

    public void setPresence(StatutPresence presence) {
        this.presence = presence;
    }

    public Adherent getAdherent() {
        return adherent;
    }

    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
    }

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        this.cours = cours;
    }

    // =========================================================================
    // Identité d'entité : equals, hashCode & toString
    // Évite toute navigation réflexive vers adherent/cours pour préserver
    // la performance et interdire les cycles infinis.
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inscription that = (Inscription) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(dateInscription, that.dateInscription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : dateInscription);
    }

    @Override
    public String toString() {
        return "Inscription{" +
                "id=" + id +
                ", dateInscription=" + dateInscription +
                ", presence=" + presence +
                ", adherentId=" + (adherent != null ? adherent.getId() : null) +
                ", coursId=" + (cours != null ? cours.getId() : null) +
                '}';
    }
}
