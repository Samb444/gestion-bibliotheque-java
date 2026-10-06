package sn.codesamb.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Représente l'emprunt d'un livre par un membre.
 */
public class Emprunt {

    private Long id;
    private final Livre livre;
    private final Membre membre;
    private final LocalDate dateEmprunt;
    private final LocalDate dateRetourPrevue;
    private LocalDate dateRetourEffective;

    /**
     * Constructeur pour initialiser un nouvel emprunt (retour non encore effectué).
     */
    public Emprunt(Long id, Livre livre, Membre membre, LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
        this(id, livre, membre, dateEmprunt, dateRetourPrevue, null);
    }

    /**
     * Constructeur complet.
     */
    public Emprunt(Long id, Livre livre, Membre membre, LocalDate dateEmprunt, LocalDate dateRetourPrevue, LocalDate dateRetourEffective) {
        this.livre = Objects.requireNonNull(livre, "Le livre est obligatoire pour un emprunt.");
        this.membre = Objects.requireNonNull(membre, "Le membre est obligatoire pour un emprunt.");
        this.dateEmprunt = Objects.requireNonNull(dateEmprunt, "La date d'emprunt est obligatoire.");
        this.dateRetourPrevue = Objects.requireNonNull(dateRetourPrevue, "La date de retour prévue est obligatoire.");

        if (dateRetourPrevue.isBefore(dateEmprunt)) {
            throw new IllegalArgumentException("La date de retour prévue ne peut pas être antérieure à la date d'emprunt.");
        }
        if (dateRetourEffective != null && dateRetourEffective.isBefore(dateEmprunt)) {
            throw new IllegalArgumentException("La date de retour effective ne peut pas être antérieure à la date d'emprunt.");
        }

        this.id = id;
        this.dateRetourEffective = dateRetourEffective;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Livre getLivre() {
        return livre;
    }

    public Membre getMembre() {
        return membre;
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public LocalDate getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public LocalDate getDateRetourEffective() {
        return dateRetourEffective;
    }

    public void setDateRetourEffective(LocalDate dateRetourEffective) {
        if (dateRetourEffective != null && dateRetourEffective.isBefore(dateEmprunt)) {
            throw new IllegalArgumentException("La date de retour effective ne peut pas être antérieure à la date d'emprunt.");
        }
        this.dateRetourEffective = dateRetourEffective;
    }

    /**
     * Détermine si l'emprunt est toujours actif (non retourné).
     *
     * @return true si le livre n'a pas encore été retourné, false sinon.
     */
    public boolean estEnCours() {
        return this.dateRetourEffective == null;
    }

    /**
     * Détermine si l'emprunt est en retard.
     * <p>
     * Un emprunt est considéré en retard si :
     * <ul>
     *     <li>la date de retour effective est absente et la date actuelle dépasse la date de retour prévue ;</li>
     *     <li>ou la date de retour effective est postérieure à la date de retour prévue.</li>
     * </ul>
     *
     * @return true si l'emprunt est en retard, false sinon.
     */
    public boolean estEnRetard() {
        if (dateRetourEffective == null) {
            return LocalDate.now().isAfter(dateRetourPrevue);
        }
        return dateRetourEffective.isAfter(dateRetourPrevue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Emprunt emprunt = (Emprunt) o;
        return id != null && Objects.equals(id, emprunt.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 31;
    }

    @Override
    public String toString() {
        return "Emprunt{" +
                "id=" + id +
                ", livre=" + livre.getTitre() +
                ", membre=" + membre.getPrenom() + " " + membre.getNom() +
                ", dateEmprunt=" + dateEmprunt +
                ", dateRetourPrevue=" + dateRetourPrevue +
                ", dateRetourEffective=" + dateRetourEffective +
                ", enCours=" + estEnCours() +
                ", enRetard=" + estEnRetard() +
                '}';
    }
}
