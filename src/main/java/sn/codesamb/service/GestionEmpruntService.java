package sn.codesamb.service;

import sn.codesamb.exception.BibliothequeException;
import sn.codesamb.exception.QuotaEmpruntDepasseException;
import sn.codesamb.model.Emprunt;
import sn.codesamb.model.EtatLivre;
import sn.codesamb.model.Livre;
import sn.codesamb.model.Membre;
import sn.codesamb.repository.EmpruntRepository;
import sn.codesamb.repository.LivreRepository;
import sn.codesamb.repository.MembreRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service métier centralisant la gestion des emprunts et retours de livres,
 * ainsi que l'application des règles associées aux quotas et aux retards.
 */
public class GestionEmpruntService {

    /**
     * Nombre maximal d'emprunts simultanés autorisés pour un membre.
     */
    public static final int QUOTA_MAX_EMPRUNTS = 3;

    /**
     * Comparateur pour ordonner les emprunts selon leur date de retour prévue chronologique.
     */
    public static final Comparator<Emprunt> COMPARATEUR_DATE_RETOUR_PREVUE =
            Comparator.comparing(Emprunt::getDateRetourPrevue);

    private final LivreRepository livreRepository;
    private final MembreRepository membreRepository;
    private final EmpruntRepository empruntRepository;

    /**
     * Initialise le service avec les repositories requis.
     *
     * @param livreRepository   le repository des livres
     * @param membreRepository  le repository des membres
     * @param empruntRepository le repository des emprunts
     */
    public GestionEmpruntService(LivreRepository livreRepository,
                                 MembreRepository membreRepository,
                                 EmpruntRepository empruntRepository) {
        this.livreRepository = Objects.requireNonNull(livreRepository, "Le livreRepository est obligatoire.");
        this.membreRepository = Objects.requireNonNull(membreRepository, "Le membreRepository est obligatoire.");
        this.empruntRepository = Objects.requireNonNull(empruntRepository, "L'empruntRepository est obligatoire.");
    }

    /**
     * Enregistre un emprunt à la date du jour.
     *
     * @param livreId          l'identifiant du livre
     * @param membreId         l'identifiant du membre
     * @param dateRetourPrevue la date de retour prévue
     * @return l'emprunt créé et persisté
     * @throws QuotaEmpruntDepasseException si le membre a déjà atteint son quota de 3 emprunts en cours
     * @throws BibliothequeException        si le livre ou le membre n'existe pas, ou si le livre est indisponible
     */
    public Emprunt enregistrerEmprunt(Long livreId, Long membreId, LocalDate dateRetourPrevue) {
        return enregistrerEmprunt(livreId, membreId, LocalDate.now(), dateRetourPrevue);
    }

    /**
     * Enregistre un emprunt avec une date d'emprunt personnalisée.
     *
     * @param livreId          l'identifiant du livre
     * @param membreId         l'identifiant du membre
     * @param dateEmprunt      la date d'emprunt
     * @param dateRetourPrevue la date de retour prévue
     * @return l'emprunt créé et persisté
     * @throws QuotaEmpruntDepasseException si le membre a déjà atteint son quota de 3 emprunts en cours
     * @throws BibliothequeException        si le livre ou le membre n'existe pas, ou si le livre est indisponible
     */
    public Emprunt enregistrerEmprunt(Long livreId, Long membreId, LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
        if (livreId == null) {
            throw new IllegalArgumentException("L'identifiant du livre est obligatoire.");
        }
        if (membreId == null) {
            throw new IllegalArgumentException("L'identifiant du membre est obligatoire.");
        }
        if (dateEmprunt == null) {
            throw new IllegalArgumentException("La date d'emprunt est obligatoire.");
        }
        if (dateRetourPrevue == null) {
            throw new IllegalArgumentException("La date de retour prévue est obligatoire.");
        }

        // Étape 1 : Vérifier que le membre existe
        Membre membre = membreRepository.findById(membreId)
                .orElseThrow(() -> new BibliothequeException("Membre introuvable avec l'identifiant : " + membreId));

        // Étape 2 : Vérifier que le livre existe
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new BibliothequeException("Livre introuvable avec l'identifiant : " + livreId));

        // Étape 3 : Vérifier que le livre est disponible
        if (!livre.estDisponible()) {
            throw new BibliothequeException("Le livre '" + livre.getTitre() + "' n'est pas disponible pour l'emprunt (état : " + livre.getEtat() + ").");
        }

        // Étape 4 & 5 : Compter les emprunts en cours du membre et vérifier le quota
        List<Emprunt> empruntsEnCours = empruntRepository.findEnCoursByMembreId(membreId);
        if (empruntsEnCours.size() >= QUOTA_MAX_EMPRUNTS) {
            throw new QuotaEmpruntDepasseException("Le membre " + membre.getPrenom() + " " + membre.getNom() + " ne peut pas avoir plus de " + QUOTA_MAX_EMPRUNTS + " emprunts simultanés.");
        }

        // Étape 6 : Créer l'objet Emprunt
        Long empruntId = empruntRepository.nextId();
        Emprunt emprunt = new Emprunt(empruntId, livre, membre, dateEmprunt, dateRetourPrevue);

        // Étape 7 : Sauvegarder l'emprunt dans EmpruntRepository
        empruntRepository.save(emprunt);

        // Étape 8 : Changer l'état du livre DISPONIBLE -> EMPRUNTE puis sauvegarder
        livre.setEtat(EtatLivre.EMPRUNTE);
        livreRepository.save(livre);

        return emprunt;
    }

    /**
     * Enregistre le retour d'un livre à la date du jour.
     *
     * @param empruntId l'identifiant de l'emprunt
     * @return l'emprunt mis à jour
     * @throws BibliothequeException si l'emprunt n'existe pas ou est déjà retourné
     */
    public Emprunt enregistrerRetour(Long empruntId) {
        return enregistrerRetour(empruntId, LocalDate.now());
    }

    /**
     * Enregistre le retour d'un livre à une date effective spécifiée.
     *
     * @param empruntId           l'identifiant de l'emprunt
     * @param dateRetourEffective la date effective de retour
     * @return l'emprunt mis à jour
     * @throws BibliothequeException si l'emprunt n'existe pas ou est déjà retourné
     */
    public Emprunt enregistrerRetour(Long empruntId, LocalDate dateRetourEffective) {
        if (empruntId == null) {
            throw new IllegalArgumentException("L'identifiant de l'emprunt est obligatoire.");
        }
        if (dateRetourEffective == null) {
            throw new IllegalArgumentException("La date de retour effective est obligatoire.");
        }

        // 1 & 2 : Rechercher l'emprunt et vérifier qu'il existe
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new BibliothequeException("Emprunt introuvable avec l'identifiant : " + empruntId));

        // 3 : Vérifier qu'il est encore en cours
        if (!emprunt.estEnCours()) {
            throw new BibliothequeException("L'emprunt n°" + empruntId + " a déjà été retourné le " + emprunt.getDateRetourEffective() + ".");
        }

        // 4 : Enregistrer la date de retour effective
        emprunt.setDateRetourEffective(dateRetourEffective);

        // 5 & 6 : Récupérer le livre concerné et remettre son état à DISPONIBLE
        Livre livre = emprunt.getLivre();
        livre.setEtat(EtatLivre.DISPONIBLE);

        // 7 : Sauvegarder les modifications
        livreRepository.save(livre);
        empruntRepository.save(emprunt);

        return emprunt;
    }

    /**
     * Récupère tous les emprunts d'un membre.
     *
     * @param membreId l'identifiant du membre
     * @return la liste de tous les emprunts effectués par le membre
     * @throws BibliothequeException si le membre n'existe pas
     */
    public List<Emprunt> findEmpruntsByMembre(Long membreId) {
        if (membreId == null) {
            throw new IllegalArgumentException("L'identifiant du membre est obligatoire.");
        }
        if (!membreRepository.existsById(membreId)) {
            throw new BibliothequeException("Membre introuvable avec l'identifiant : " + membreId);
        }
        return empruntRepository.findByMembreId(membreId);
    }

    /**
     * Récupère tous les emprunts d'un membre triés selon un comparateur fourni.
     *
     * @param membreId   l'identifiant du membre
     * @param comparator le comparateur définissant l'ordre de tri
     * @return la liste des emprunts triés du membre
     * @throws BibliothequeException si le membre n'existe pas
     */
    public List<Emprunt> findEmpruntsByMembre(Long membreId, Comparator<Emprunt> comparator) {
        if (membreId == null) {
            throw new IllegalArgumentException("L'identifiant du membre est obligatoire.");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("Le comparateur est obligatoire.");
        }
        if (!membreRepository.existsById(membreId)) {
            throw new BibliothequeException("Membre introuvable avec l'identifiant : " + membreId);
        }
        return empruntRepository.findByMembreId(membreId).stream()
                .sorted(comparator)
                .toList();
    }

    /**
     * Récupère tous les emprunts d'un membre triés par date de retour prévue (ordre chronologique croissant).
     *
     * @param membreId l'identifiant du membre
     * @return la liste des emprunts triés par date de retour prévue
     * @throws BibliothequeException si le membre n'existe pas
     */
    public List<Emprunt> findEmpruntsByMembreTriesParDateRetourPrevue(Long membreId) {
        return findEmpruntsByMembre(membreId, COMPARATEUR_DATE_RETOUR_PREVUE);
    }

    /**
     * Récupère les emprunts en cours (non retournés) d'un membre.
     *
     * @param membreId l'identifiant du membre
     * @return la liste des emprunts actuellement en cours pour le membre
     * @throws BibliothequeException si le membre n'existe pas
     */
    public List<Emprunt> findEmpruntsEnCoursByMembre(Long membreId) {
        if (membreId == null) {
            throw new IllegalArgumentException("L'identifiant du membre est obligatoire.");
        }
        if (!membreRepository.existsById(membreId)) {
            throw new BibliothequeException("Membre introuvable avec l'identifiant : " + membreId);
        }
        return empruntRepository.findEnCoursByMembreId(membreId);
    }

    /**
     * Récupère la liste des emprunts toujours en cours dont la date de retour prévue est dépassée.
     *
     * @return la liste des emprunts en retard
     */
    public List<Emprunt> findEmpruntsEnRetard() {
        return empruntRepository.findEnRetard();
    }
}
