package sn.codesamb;

import sn.codesamb.exception.BibliothequeException;
import sn.codesamb.model.CategorieLivre;
import sn.codesamb.model.Emprunt;
import sn.codesamb.model.Livre;
import sn.codesamb.model.Membre;
import sn.codesamb.repository.EmpruntRepository;
import sn.codesamb.repository.LivreRepository;
import sn.codesamb.repository.MembreRepository;
import sn.codesamb.service.GestionEmpruntService;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Point d'entrée de l'application console de gestion de bibliothèque.
 * <p>
 * Cette classe gère l'interface utilisateur en ligne de commande :
 * affichage du menu, capture et validation des entrées utilisateur,
 * et délégation stricte des traitements métier au {@link GestionEmpruntService}.
 */
public class App {

    private final GestionEmpruntService service;
    private final Scanner scanner;
    private final PrintStream out;

    /**
     * Initialise l'application console avec le service et les flux d'E/S spécifiés.
     *
     * @param service le service métier de gestion des emprunts
     * @param scanner le scanner pour la saisie des données
     * @param out     le flux d'affichage
     */
    public App(GestionEmpruntService service, Scanner scanner, PrintStream out) {
        this.service = Objects.requireNonNull(service, "Le service métier est obligatoire.");
        this.scanner = Objects.requireNonNull(scanner, "Le scanner est obligatoire.");
        this.out = Objects.requireNonNull(out, "Le flux de sortie est obligatoire.");
    }

    /**
     * Initialise l'application console avec la sortie standard.
     *
     * @param service le service métier de gestion des emprunts
     * @param scanner le scanner pour la saisie des données
     */
    public App(GestionEmpruntService service, Scanner scanner) {
        this(service, scanner, System.out);
    }

    /**
     * Point d'entrée principal.
     *
     * @param args arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        LivreRepository livreRepository = new LivreRepository();
        MembreRepository membreRepository = new MembreRepository();
        EmpruntRepository empruntRepository = new EmpruntRepository();
        GestionEmpruntService service = new GestionEmpruntService(livreRepository, membreRepository, empruntRepository);

        initialiserDonneesDemo(livreRepository, membreRepository, service);

        try (Scanner scanner = new Scanner(System.in)) {
            App app = new App(service, scanner);
            app.demarrer();
        }
    }

    /**
     * Démarre la boucle principale du menu console.
     */
    public void demarrer() {
        out.println("=================================");
        out.println("     GESTION DE BIBLIOTHÈQUE     ");
        out.println("=================================");
        out.println("Bienvenue dans le système de gestion de bibliothèque.");
        out.println("[Données de démo chargées : membres (ID 1-4), livres (ID 1-7)]");

        boolean continuer = true;

        while (continuer) {
            afficherMenu();
            Integer choix = lireChoixMenu();

            if (choix == null) {
                // Fin de flux (EOF)
                break;
            }

            switch (choix) {
                case 1 -> enregistrerEmprunt();
                case 2 -> enregistrerRetour();
                case 3 -> listerEmpruntsMembre();
                case 4 -> listerLivresEnRetard();
                case 0 -> {
                    continuer = false;
                    out.println("\nMerci d'avoir utilisé l'application. Au revoir !");
                }
                default -> out.println("\nOption invalide. Veuillez saisir un numéro d'option valide (1, 2, 3, 4 ou 0).");
            }
        }
    }

    /**
     * Affiche le menu principal.
     */
    private void afficherMenu() {
        out.println("\n=================================");
        out.println("GESTION DE BIBLIOTHÈQUE");
        out.println("=================================");
        out.println("1. Enregistrer un emprunt");
        out.println("2. Enregistrer un retour");
        out.println("3. Lister les emprunts d'un membre");
        out.println("4. Lister les livres en retard");
        out.println("5. Quitter");
        out.println("=================================");
    }

    /**
     * Action 1 : Enregistrer un emprunt.
     */
    private void enregistrerEmprunt() {
        out.println("\n--- Enregistrer un emprunt ---");

        Long membreId = lireLong("Identifiant du membre : ");
        if (membreId == null) return;

        Long livreId = lireLong("Identifiant du livre : ");
        if (livreId == null) return;

        LocalDate dateRetourPrevue = lireDate("Date de retour prévue (AAAA-MM-JJ, ex: 2026-10-20) : ");
        if (dateRetourPrevue == null) return;

        try {
            Emprunt emprunt = service.enregistrerEmprunt(livreId, membreId, dateRetourPrevue);
            out.println("\n✓ Emprunt enregistré avec succès !");
            out.println("  • ID Emprunt            : " + emprunt.getId());
            out.println("  • Livre                 : " + emprunt.getLivre().getTitre() + " (ID: " + emprunt.getLivre().getId() + ")");
            out.println("  • Membre                : " + emprunt.getMembre().getPrenom() + " " + emprunt.getMembre().getNom() + " (ID: " + emprunt.getMembre().getId() + ")");
            out.println("  • Date d'emprunt        : " + emprunt.getDateEmprunt());
            out.println("  • Date de retour prévue : " + emprunt.getDateRetourPrevue());
        } catch (BibliothequeException e) {
            out.println("\nErreur métier : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println("\nErreur de saisie : " + e.getMessage());
        }
    }

    /**
     * Action 2 : Enregistrer un retour.
     */
    private void enregistrerRetour() {
        out.println("\n--- Enregistrer un retour ---");

        Long empruntId = lireLong("Identifiant de l'emprunt : ");
        if (empruntId == null) return;

        try {
            Emprunt emprunt = service.enregistrerRetour(empruntId);
            out.println("\n✓ Retour enregistré avec succès !");
            out.println("  • ID Emprunt            : " + emprunt.getId());
            out.println("  • Livre                 : " + emprunt.getLivre().getTitre() + " (ID: " + emprunt.getLivre().getId() + ")");
            out.println("  • Membre                : " + emprunt.getMembre().getPrenom() + " " + emprunt.getMembre().getNom());
            out.println("  • Date de retour        : " + emprunt.getDateRetourEffective());
            out.println("  • État du livre         : " + emprunt.getLivre().getEtat());
        } catch (BibliothequeException e) {
            out.println("\nErreur métier : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println("\nErreur de saisie : " + e.getMessage());
        }
    }

    /**
     * Action 3 : Lister les emprunts d'un membre.
     */
    private void listerEmpruntsMembre() {
        out.println("\n--- Lister les emprunts d'un membre ---");

        Long membreId = lireLong("Identifiant du membre : ");
        if (membreId == null) return;

        try {
            List<Emprunt> emprunts = service.findEmpruntsByMembreTriesParDateRetourPrevue(membreId);
            if (emprunts.isEmpty()) {
                out.println("\nAucun emprunt trouvé pour le membre n°" + membreId + ".");
            } else {
                out.println("\nEmprunts du membre n°" + membreId + " (" + emprunts.size() + " emprunt(s)) :");
                for (Emprunt e : emprunts) {
                    String statut;
                    if (e.estEnCours()) {
                        statut = e.estEnRetard() ? "EN COURS (EN RETARD)" : "EN COURS";
                    } else {
                        statut = "RETOURNÉ (le " + e.getDateRetourEffective() + ")";
                    }
                    out.println("----------------------------------------");
                    out.println("  ID Emprunt            : " + e.getId());
                    out.println("  Livre                 : " + e.getLivre().getTitre() + " (ID: " + e.getLivre().getId() + ")");
                    out.println("  Date d'emprunt        : " + e.getDateEmprunt());
                    out.println("  Date de retour prévue : " + e.getDateRetourPrevue());
                    out.println("  Date retour effectif  : " + (e.getDateRetourEffective() != null ? e.getDateRetourEffective() : "Non retourné"));
                    out.println("  Statut                : " + statut);
                }
                out.println("----------------------------------------");
            }
        } catch (BibliothequeException e) {
            out.println("\nErreur métier : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println("\nErreur de saisie : " + e.getMessage());
        }
    }

    /**
     * Action 4 : Lister les livres en retard.
     */
    private void listerLivresEnRetard() {
        out.println("\n--- Liste des livres en retard ---");

        List<Emprunt> retards = service.findEmpruntsEnRetard();

        if (retards.isEmpty()) {
            out.println("\nAucun livre en retard actuellement.");
        } else {
            out.println("\n" + retards.size() + " livre(s) actuellement en retard :");
            for (Emprunt e : retards) {
                long joursRetard = ChronoUnit.DAYS.between(e.getDateRetourPrevue(), LocalDate.now());
                out.println("----------------------------------------");
                out.println("  ID Emprunt            : " + e.getId());
                out.println("  ID Livre              : " + e.getLivre().getId());
                out.println("  Titre du livre        : " + e.getLivre().getTitre());
                out.println("  Membre                : " + e.getMembre().getPrenom() + " " + e.getMembre().getNom() + " (ID: " + e.getMembre().getId() + ")");
                out.println("  Date de retour prévue : " + e.getDateRetourPrevue());
                out.println("  Jours de retard       : " + (joursRetard > 0 ? joursRetard + " jour(s)" : "0 jour"));
            }
            out.println("----------------------------------------");
        }
    }

    /**
     * Lit et parse le choix du menu de manière sécurisée.
     *
     * @return le numéro d'option choisi, -1 en cas d'entrée invalide, ou null si fin de flux
     */
    private Integer lireChoixMenu() {
        out.print("Choisissez une option : ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String ligne = scanner.nextLine().trim();
        if (ligne.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(ligne);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Lit un identifiant numérique (Long) avec gestion des erreurs de format.
     *
     * @param invite le texte d'invite
     * @return l'identifiant saisi, ou null si fin de flux
     */
    private Long lireLong(String invite) {
        while (true) {
            out.print(invite);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String ligne = scanner.nextLine().trim();
            if (ligne.isEmpty()) {
                out.println("Erreur : Ce champ est obligatoire.");
                continue;
            }
            try {
                long valeur = Long.parseLong(ligne);
                if (valeur <= 0) {
                    out.println("Erreur : L'identifiant doit être un nombre strictement positif.");
                    continue;
                }
                return valeur;
            } catch (NumberFormatException e) {
                out.println("Erreur : Saisie non numérique. Veuillez entrer un identifiant numérique valide.");
            }
        }
    }

    /**
     * Lit une date au format ISO (AAAA-MM-JJ) avec validation stricte.
     *
     * @param invite le texte d'invite
     * @return la date saisie, ou null si fin de flux
     */
    private LocalDate lireDate(String invite) {
        while (true) {
            out.print(invite);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String ligne = scanner.nextLine().trim();
            if (ligne.isEmpty()) {
                out.println("Erreur : La date est obligatoire.");
                continue;
            }
            try {
                return LocalDate.parse(ligne, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                out.println("Erreur : Format de date invalide. Format attendu : AAAA-MM-JJ (ex : 2026-10-20).");
            }
        }
    }

    /**
     * Initialise un jeu de données de démonstration cohérent pour tester l'application console.
     *
     * @param livreRepo   le repository des livres
     * @param membreRepo  le repository des membres
     * @param service     le service métier pour créer les emprunts initiaux
     */
    public static void initialiserDonneesDemo(LivreRepository livreRepo,
                                              MembreRepository membreRepo,
                                              GestionEmpruntService service) {
        // Membres de démonstration
        Membre m1 = new Membre(1L, "Diallo", "Amadou", "amadou.diallo@email.sn");
        Membre m2 = new Membre(2L, "Ndiaye", "Fatou", "fatou.ndiaye@email.sn");
        Membre m3 = new Membre(3L, "Sow", "Moussa", "moussa.sow@email.sn");
        Membre m4 = new Membre(4L, "Ba", "Aïssatou", "aissatou.ba@email.sn");
        membreRepo.save(m1);
        membreRepo.save(m2);
        membreRepo.save(m3);
        membreRepo.save(m4);

        // Livres de démonstration
        Livre l1 = new Livre(1L, "Les Misérables", "Victor Hugo", CategorieLivre.ROMAN);
        Livre l2 = new Livre(2L, "Une si longue lettre", "Mariama Bâ", CategorieLivre.ROMAN);
        Livre l3 = new Livre(3L, "Introduction aux algorithmes", "Thomas Cormen", CategorieLivre.INFORMATIQUE);
        Livre l4 = new Livre(4L, "Clean Code", "Robert C. Martin", CategorieLivre.INFORMATIQUE);
        Livre l5 = new Livre(5L, "Histoire du Sénégal", "Cheikh Anta Diop", CategorieLivre.HISTOIRE);
        Livre l6 = new Livre(6L, "Astrophysique pour les gens pressés", "Neil deGrasse Tyson", CategorieLivre.SCIENCE);
        Livre l7 = new Livre(7L, "Droit constitutionnel contemporain", "Michel Verpeaux", CategorieLivre.DROIT);
        livreRepo.save(l1);
        livreRepo.save(l2);
        livreRepo.save(l3);
        livreRepo.save(l4);
        livreRepo.save(l5);
        livreRepo.save(l6);
        livreRepo.save(l7);

        // Emprunt 1 : en retard (date emprunt : il y a 20 jours, retour prévu : il y a 5 jours)
        service.enregistrerEmprunt(1L, 1L, LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));

        // Emprunt 2 : en cours normal (date emprunt : il y a 2 jours, retour prévu : dans 14 jours)
        service.enregistrerEmprunt(3L, 2L, LocalDate.now().minusDays(2), LocalDate.now().plusDays(14));
    }
}
