package sn.codesamb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.codesamb.model.CategorieLivre;
import sn.codesamb.model.Emprunt;
import sn.codesamb.model.Livre;
import sn.codesamb.model.Membre;
import sn.codesamb.repository.EmpruntRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires du service statistique (Stream)")
class StatistiqueBibliothequeServiceTest {

    private EmpruntRepository empruntRepository;
    private StatistiqueBibliothequeService statistiqueService;

    private Membre membre1;
    private Membre membre2;

    @BeforeEach
    void setUp() {
        empruntRepository = new EmpruntRepository();
        statistiqueService = new StatistiqueBibliothequeService(empruntRepository);

        membre1 = new Membre(1L, "Diop", "Amadou", "amadou@example.com");
        membre2 = new Membre(2L, "Fall", "Awa", "awa@example.com");
    }

    @Test
    @DisplayName("Regroupement et comptage : les catégories sont correctement regroupées et comptées")
    void testRegroupementEtComptageCorrect() {
        // 2 ROMAN, 3 INFORMATIQUE, 1 HISTOIRE
        Livre roman1 = new Livre(1L, "Les Misérables", "Victor Hugo", CategorieLivre.ROMAN);
        Livre roman2 = new Livre(2L, "1984", "George Orwell", CategorieLivre.ROMAN);
        Livre info1 = new Livre(3L, "Clean Code", "Robert Martin", CategorieLivre.INFORMATIQUE);
        Livre info2 = new Livre(4L, "Effective Java", "Joshua Bloch", CategorieLivre.INFORMATIQUE);
        Livre info3 = new Livre(5L, "Refactoring", "Martin Fowler", CategorieLivre.INFORMATIQUE);
        Livre hist1 = new Livre(6L, "Histoire générale", "Cheikh Anta Diop", CategorieLivre.HISTOIRE);

        LocalDate today = LocalDate.now();
        empruntRepository.save(new Emprunt(1L, roman1, membre1, today, today.plusDays(10)));
        empruntRepository.save(new Emprunt(2L, roman2, membre2, today, today.plusDays(12)));
        empruntRepository.save(new Emprunt(3L, info1, membre1, today, today.plusDays(14)));
        empruntRepository.save(new Emprunt(4L, info2, membre2, today, today.plusDays(15)));
        empruntRepository.save(new Emprunt(5L, info3, membre1, today, today.plusDays(16)));
        empruntRepository.save(new Emprunt(6L, hist1, membre2, today, today.plusDays(20)));

        Map<CategorieLivre, Long> stats = statistiqueService.compterEmpruntsParCategorie();

        assertNotNull(stats);
        assertEquals(3, stats.size(), "Doit contenir exactement 3 catégories distinctes ayant des emprunts.");
        assertEquals(2L, stats.get(CategorieLivre.ROMAN));
        assertEquals(3L, stats.get(CategorieLivre.INFORMATIQUE));
        assertEquals(1L, stats.get(CategorieLivre.HISTOIRE));
    }

    @Test
    @DisplayName("Catégories sans emprunt : non incluses dans la map de statistiques")
    void testCategorieSansEmpruntNonComptabilisee() {
        Livre roman = new Livre(1L, "Une si longue lettre", "Mariama Bâ", CategorieLivre.ROMAN);
        LocalDate today = LocalDate.now();
        empruntRepository.save(new Emprunt(1L, roman, membre1, today, today.plusDays(10)));

        Map<CategorieLivre, Long> stats = statistiqueService.compterEmpruntsParCategorie();

        assertEquals(1, stats.size());
        assertTrue(stats.containsKey(CategorieLivre.ROMAN));
        assertFalse(stats.containsKey(CategorieLivre.SCIENCE), "SCIENCE n'a aucun emprunt et ne doit pas figurer.");
        assertFalse(stats.containsKey(CategorieLivre.DROIT), "DROIT n'a aucun emprunt et ne doit pas figurer.");
        assertFalse(stats.containsKey(CategorieLivre.HISTOIRE), "HISTOIRE n'a aucun emprunt et ne doit pas figurer.");
        assertFalse(stats.containsKey(CategorieLivre.INFORMATIQUE), "INFORMATIQUE n'a aucun emprunt et ne doit pas figurer.");
    }

    @Test
    @DisplayName("Plusieurs livres d'une même catégorie : correctement comptés")
    void testPlusieursLivresMemeCategorie() {
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 5; i++) {
            Livre livre = new Livre((long) i, "Livre Info " + i, "Auteur " + i, CategorieLivre.INFORMATIQUE);
            empruntRepository.save(new Emprunt((long) i, livre, membre1, today, today.plusDays(i)));
        }

        Map<CategorieLivre, Long> stats = statistiqueService.compterEmpruntsParCategorie();

        assertEquals(1, stats.size());
        assertEquals(5L, stats.get(CategorieLivre.INFORMATIQUE));
    }

    @Test
    @DisplayName("Aucun emprunt : retourne une map vide sans erreur")
    void testAucunEmprunt() {
        Map<CategorieLivre, Long> stats = statistiqueService.compterEmpruntsParCategorie();

        assertNotNull(stats);
        assertTrue(stats.isEmpty(), "La map de statistiques doit être vide lorsqu'aucun emprunt n'existe.");
    }

    @Test
    @DisplayName("Comptage avec liste d'emprunts explicite")
    void testCompterEmpruntsAvecListeExplicite() {
        Livre livre1 = new Livre(1L, "Titre 1", "Auteur 1", CategorieLivre.DROIT);
        Livre livre2 = new Livre(2L, "Titre 2", "Auteur 2", CategorieLivre.DROIT);
        LocalDate today = LocalDate.now();

        Emprunt e1 = new Emprunt(1L, livre1, membre1, today, today.plusDays(7));
        Emprunt e2 = new Emprunt(2L, livre2, membre2, today, today.plusDays(7));

        Map<CategorieLivre, Long> stats = statistiqueService.compterEmpruntsParCategorie(List.of(e1, e2));

        assertEquals(1, stats.size());
        assertEquals(2L, stats.get(CategorieLivre.DROIT));
    }

    @Test
    @DisplayName("Comptage avec liste nulle : lève IllegalArgumentException")
    void testCompterEmpruntsAvecListeNulle() {
        assertThrows(IllegalArgumentException.class, () -> statistiqueService.compterEmpruntsParCategorie(null));
    }

    @Test
    @DisplayName("Comptage des emprunts en cours uniquement par catégorie")
    void testCompterEmpruntsEnCoursParCategorie() {
        Livre roman = new Livre(1L, "Roman 1", "Auteur 1", CategorieLivre.ROMAN);
        Livre science = new Livre(2L, "Science 1", "Auteur 2", CategorieLivre.SCIENCE);
        LocalDate today = LocalDate.now();

        // 1 emprunt ROMAN déjà retourné
        Emprunt empruntRetourne = new Emprunt(1L, roman, membre1, today.minusDays(10), today, today.minusDays(2));
        // 1 emprunt SCIENCE en cours
        Emprunt empruntEnCours = new Emprunt(2L, science, membre2, today.minusDays(5), today.plusDays(5));

        empruntRepository.save(empruntRetourne);
        empruntRepository.save(empruntEnCours);

        Map<CategorieLivre, Long> statsEnCours = statistiqueService.compterEmpruntsEnCoursParCategorie();

        assertEquals(1, statsEnCours.size());
        assertEquals(1L, statsEnCours.get(CategorieLivre.SCIENCE));
        assertNull(statsEnCours.get(CategorieLivre.ROMAN), "L'emprunt ROMAN retourné ne doit pas être inclus.");
    }

    @Test
    @DisplayName("Tri des statistiques par volume décroissant (Comparator sur les entrées de Map)")
    void testGetStatistiquesTrieesParNombreDecroissant() {
        Livre roman = new Livre(1L, "Roman", "Auteur", CategorieLivre.ROMAN);
        Livre info1 = new Livre(2L, "Info 1", "Auteur", CategorieLivre.INFORMATIQUE);
        Livre info2 = new Livre(3L, "Info 2", "Auteur", CategorieLivre.INFORMATIQUE);
        Livre hist = new Livre(4L, "Histoire", "Auteur", CategorieLivre.HISTOIRE);
        LocalDate today = LocalDate.now();

        empruntRepository.save(new Emprunt(1L, roman, membre1, today, today.plusDays(5)));
        empruntRepository.save(new Emprunt(2L, info1, membre1, today, today.plusDays(5)));
        empruntRepository.save(new Emprunt(3L, info2, membre2, today, today.plusDays(5)));

        List<Map.Entry<CategorieLivre, Long>> triees = statistiqueService.getStatistiquesTrieesParNombreDecroissant();

        assertEquals(2, triees.size());
        assertEquals(CategorieLivre.INFORMATIQUE, triees.get(0).getKey());
        assertEquals(2L, triees.get(0).getValue());
        assertEquals(CategorieLivre.ROMAN, triees.get(1).getKey());
        assertEquals(1L, triees.get(1).getValue());
    }

    @Test
    @DisplayName("Formatage textuel du rapport de statistiques")
    void testFormaterStatistiquesParCategorie() {
        Livre droit = new Livre(1L, "Droit", "Auteur", CategorieLivre.DROIT);
        LocalDate today = LocalDate.now();
        empruntRepository.save(new Emprunt(1L, droit, membre1, today, today.plusDays(5)));

        String rapport = statistiqueService.formaterStatistiquesParCategorie();

        assertNotNull(rapport);
        assertTrue(rapport.contains("DROIT : 1"));
    }

    @Test
    @DisplayName("Initialisation avec repository null lève NullPointerException")
    void testConstructeurRepositoryNull() {
        assertThrows(NullPointerException.class, () -> new StatistiqueBibliothequeService(null));
    }
}
