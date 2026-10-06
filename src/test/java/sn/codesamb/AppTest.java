package sn.codesamb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.codesamb.repository.EmpruntRepository;
import sn.codesamb.repository.LivreRepository;
import sn.codesamb.repository.MembreRepository;
import sn.codesamb.service.GestionEmpruntService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests de l'application console App")
class AppTest {

    private LivreRepository livreRepository;
    private MembreRepository membreRepository;
    private EmpruntRepository empruntRepository;
    private GestionEmpruntService service;

    @BeforeEach
    void setUp() {
        livreRepository = new LivreRepository();
        membreRepository = new MembreRepository();
        empruntRepository = new EmpruntRepository();
        service = new GestionEmpruntService(livreRepository, membreRepository, empruntRepository);
        App.initialiserDonneesDemo(livreRepository, membreRepository, service);
    }

    private String executerAppAvecEntree(String simulationEntree) {
        Scanner scanner = new Scanner(simulationEntree);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos, true, StandardCharsets.UTF_8);

        App app = new App(service, scanner, out);
        assertDoesNotThrow(app::demarrer);

        return baos.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Menu principal : Quitter immédiatement avec l'option 0 et vérifier le menu affiché")
    void testQuitterAvecOption0() {
        String sortie = executerAppAvecEntree("0\n");

        assertTrue(sortie.contains("GESTION DE BIBLIOTHÈQUE"));
        assertTrue(sortie.contains("1. Enregistrer un emprunt"));
        assertTrue(sortie.contains("2. Enregistrer un retour"));
        assertTrue(sortie.contains("3. Lister les emprunts d'un membre"));
        assertTrue(sortie.contains("4. Lister les livres en retard"));
        assertTrue(sortie.contains("5. Quitter"));
        assertTrue(sortie.contains("Au revoir !"));
    }

    @Test
    @DisplayName("Menu principal : L'option 5 n'est plus acceptée pour quitter (rejetée comme invalide)")
    void testOption5NeQuittePlus() {
        // La saisie de '5' doit être rejetée comme option invalide, seule l'option '0' permet de quitter
        String sortie = executerAppAvecEntree("5\n0\n");

        assertTrue(sortie.contains("Option invalide. Veuillez saisir un numéro d'option valide (1, 2, 3, 4 ou 0)."));
        assertTrue(sortie.contains("Au revoir !"));
    }

    @Test
    @DisplayName("Option invalide puis quitter : ne doit pas planter")
    void testOptionInvalidePuisQuitter() {
        String sortie = executerAppAvecEntree("9\ninconnu\n0\n");

        assertTrue(sortie.contains("Option invalide"));
        assertTrue(sortie.contains("Au revoir !"));
    }

    @Test
    @DisplayName("Action 1 : Enregistrer un emprunt valide puis vérifier confirmation")
    void testEnregistrerEmpruntValide() {
        // Membre 3 emprunte Livre 2 (disponible) jusqu'au lendemain du jour d'emprunt
        String dateRetour = LocalDate.now().plusDays(7).toString();
        String entree = "1\n3\n2\n" + dateRetour + "\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Emprunt enregistré avec succès"));
        assertTrue(sortie.contains("Une si longue lettre"));
        assertTrue(sortie.contains("Moussa Sow"));
    }

    @Test
    @DisplayName("Action 1 : Tenter d'emprunter un livre déjà emprunté affiche une erreur métier sans planter")
    void testEmprunterLivreDejaEmprunte() {
        // Livre 1 est déjà emprunté dans les données de démo
        String dateRetour = LocalDate.now().plusDays(7).toString();
        String entree = "1\n4\n1\n" + dateRetour + "\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Erreur métier"));
        assertTrue(sortie.contains("n'est pas disponible pour l'emprunt"));
    }

    @Test
    @DisplayName("Action 2 : Enregistrer un retour valide puis vérifier confirmation")
    void testEnregistrerRetourValide() {
        // Emprunt 2 est en cours dans les données de démo
        String entree = "2\n2\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Retour enregistré avec succès"));
        assertTrue(sortie.contains("Introduction aux algorithmes"));
        assertTrue(sortie.contains("Disponible"));
    }

    @Test
    @DisplayName("Action 2 : Retourner un emprunt déjà retourné affiche une erreur métier sans planter")
    void testRetournerEmpruntDejaRetourne() {
        // Retourner l'emprunt 2 une première fois, puis une seconde fois
        String entree = "2\n2\n2\n2\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Retour enregistré avec succès"));
        assertTrue(sortie.contains("Erreur métier : L'emprunt n°2 a déjà été retourné"));
    }

    @Test
    @DisplayName("Action 3 : Lister les emprunts d'un membre")
    void testListerEmpruntsMembre() {
        // Membre 1 a l'emprunt 1
        String entree = "3\n1\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Emprunts du membre n°1"));
        assertTrue(sortie.contains("Les Misérables"));
        assertTrue(sortie.contains("EN COURS (EN RETARD)"));
    }

    @Test
    @DisplayName("Action 3 : Lister les emprunts d'un membre inexistant affiche une erreur")
    void testListerEmpruntsMembreInexistant() {
        String entree = "3\n999\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Membre introuvable avec l'identifiant : 999"));
    }

    @Test
    @DisplayName("Action 4 : Lister les livres en retard")
    void testListerLivresEnRetard() {
        String entree = "4\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Liste des livres en retard"));
        assertTrue(sortie.contains("Les Misérables"));
        assertTrue(sortie.contains("Amadou Diallo"));
        assertTrue(sortie.contains("jour(s)"));
    }

    @Test
    @DisplayName("Robustesse : Saisie non numérique et format de date incorrect redemandés proprement")
    void testRobustesseSaisieNonNumeriqueEtDateInvalide() {
        // Saisie option 1
        // Membre ID : 'abc' puis '3'
        // Livre ID : 'xyz' puis '4' (Clean Code, disponible)
        // Date : 'date-fausse' puis valide
        String dateValide = LocalDate.now().plusDays(10).toString();
        String entree = "1\nabc\n3\nxyz\n4\ndate-fausse\n" + dateValide + "\n0\n";

        String sortie = executerAppAvecEntree(entree);

        assertTrue(sortie.contains("Saisie non numérique. Veuillez entrer un identifiant numérique valide."));
        assertTrue(sortie.contains("Format de date invalide. Format attendu : AAAA-MM-JJ"));
        assertTrue(sortie.contains("Emprunt enregistré avec succès"));
    }
}
