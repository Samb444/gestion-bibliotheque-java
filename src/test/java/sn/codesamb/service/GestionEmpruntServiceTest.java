package sn.codesamb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.codesamb.exception.BibliothequeException;
import sn.codesamb.exception.QuotaEmpruntDepasseException;
import sn.codesamb.model.CategorieLivre;
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

import static org.junit.jupiter.api.Assertions.*;

class GestionEmpruntServiceTest {

    private LivreRepository livreRepository;
    private MembreRepository membreRepository;
    private EmpruntRepository empruntRepository;
    private GestionEmpruntService service;

    private Livre livre1;
    private Livre livre2;
    private Livre livre3;
    private Livre livre4;
    private Membre membre1;
    private Membre membre2;

    @BeforeEach
    void setUp() {
        livreRepository = new LivreRepository();
        membreRepository = new MembreRepository();
        empruntRepository = new EmpruntRepository();
        service = new GestionEmpruntService(livreRepository, membreRepository, empruntRepository);

        livre1 = new Livre(1L, "Le Petit Prince", "Antoine de Saint-Exupéry", CategorieLivre.ROMAN);
        livre2 = new Livre(2L, "Clean Code", "Robert C. Martin", CategorieLivre.INFORMATIQUE);
        livre3 = new Livre(3L, "1984", "George Orwell", CategorieLivre.ROMAN);
        livre4 = new Livre(4L, "Sapiens", "Yuval Noah Harari", CategorieLivre.HISTOIRE);

        livreRepository.save(livre1);
        livreRepository.save(livre2);
        livreRepository.save(livre3);
        livreRepository.save(livre4);

        membre1 = new Membre(1L, "Diop", "Amadou", "amadou@example.com");
        membre2 = new Membre(2L, "Fall", "Awa", "awa@example.com");

        membreRepository.save(membre1);
        membreRepository.save(membre2);
    }

    @Test
    @DisplayName("Cas normal : enregistrer un emprunt avec un membre existant et un livre disponible")
    void testEnregistrerEmpruntCasNormal() {
        LocalDate dateRetourPrevue = LocalDate.now().plusDays(14);
        Emprunt emprunt = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), dateRetourPrevue);

        assertNotNull(emprunt);
        assertNotNull(emprunt.getId());
        assertEquals(livre1.getId(), emprunt.getLivre().getId());
        assertEquals(membre1.getId(), emprunt.getMembre().getId());
        assertEquals(LocalDate.now(), emprunt.getDateEmprunt());
        assertEquals(dateRetourPrevue, emprunt.getDateRetourPrevue());
        assertNull(emprunt.getDateRetourEffective());
        assertTrue(emprunt.estEnCours());

        // Vérification de la persistance de l'emprunt
        assertTrue(empruntRepository.existsById(emprunt.getId()));

        // Vérification que l'état du livre est passé à EMPRUNTE
        Livre livreMaj = livreRepository.findById(livre1.getId()).orElseThrow();
        assertEquals(EtatLivre.EMPRUNTE, livreMaj.getEtat());
        assertFalse(livreMaj.estDisponible());
    }

    @Test
    @DisplayName("Livre déjà emprunté : refuser un second emprunt sur le même livre")
    void testEmprunterLivreDejaEmprunte() {
        service.enregistrerEmprunt(livre1.getId(), membre1.getId(), LocalDate.now().plusDays(14));

        // Tenter d'emprunter à nouveau livre1
        BibliothequeException exception = assertThrows(
                BibliothequeException.class,
                () -> service.enregistrerEmprunt(livre1.getId(), membre2.getId(), LocalDate.now().plusDays(7))
        );

        assertTrue(exception.getMessage().contains("pas disponible"));
    }

    @Test
    @DisplayName("Limite des 3 emprunts : refuser le 4ème emprunt pour le même membre")
    void testLimiteDesTroisEmprunts() {
        LocalDate echeance = LocalDate.now().plusDays(14);

        // 3 emprunts acceptés
        service.enregistrerEmprunt(livre1.getId(), membre1.getId(), echeance);
        service.enregistrerEmprunt(livre2.getId(), membre1.getId(), echeance);
        service.enregistrerEmprunt(livre3.getId(), membre1.getId(), echeance);

        assertEquals(3, empruntRepository.findEnCoursByMembreId(membre1.getId()).size());

        // 4ème emprunt refusé avec QuotaEmpruntDepasseException
        QuotaEmpruntDepasseException exception = assertThrows(
                QuotaEmpruntDepasseException.class,
                () -> service.enregistrerEmprunt(livre4.getId(), membre1.getId(), echeance)
        );

        assertTrue(exception.getMessage().contains("ne peut pas avoir plus de 3 emprunts"));
    }

    @Test
    @DisplayName("Membres différents : la limite de 3 emprunts s'applique par membre et non globalement")
    void testLimiteTroisEmpruntsParMembre() {
        LocalDate echeance = LocalDate.now().plusDays(14);

        // Membre 1 effectue 3 emprunts
        service.enregistrerEmprunt(livre1.getId(), membre1.getId(), echeance);
        service.enregistrerEmprunt(livre2.getId(), membre1.getId(), echeance);
        service.enregistrerEmprunt(livre3.getId(), membre1.getId(), echeance);

        // Membre 2 peut quand même emprunter un 4ème livre de la bibliothèque
        Emprunt empruntMembre2 = service.enregistrerEmprunt(livre4.getId(), membre2.getId(), echeance);

        assertNotNull(empruntMembre2);
        assertEquals(3, empruntRepository.findEnCoursByMembreId(membre1.getId()).size());
        assertEquals(1, empruntRepository.findEnCoursByMembreId(membre2.getId()).size());
    }

    @Test
    @DisplayName("Retour : enregistrer le retour d'un livre et vérifier son état DISPONIBLE")
    void testEnregistrerRetourCasNormal() {
        Emprunt emprunt = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), LocalDate.now().plusDays(14));

        Emprunt empruntRetourne = service.enregistrerRetour(emprunt.getId());

        assertNotNull(empruntRetourne.getDateRetourEffective());
        assertEquals(LocalDate.now(), empruntRetourne.getDateRetourEffective());
        assertFalse(empruntRetourne.estEnCours());

        // Livre redevient DISPONIBLE dans le repository
        Livre livreMaj = livreRepository.findById(livre1.getId()).orElseThrow();
        assertEquals(EtatLivre.DISPONIBLE, livreMaj.getEtat());
        assertTrue(livreMaj.estDisponible());
    }

    @Test
    @DisplayName("Double retour : refuser le retour d'un emprunt déjà retourné")
    void testRefuserDoubleRetour() {
        Emprunt emprunt = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), LocalDate.now().plusDays(14));
        service.enregistrerRetour(emprunt.getId());

        BibliothequeException exception = assertThrows(
                BibliothequeException.class,
                () -> service.enregistrerRetour(emprunt.getId())
        );

        assertTrue(exception.getMessage().contains("déjà été retourné"));
    }

    @Test
    @DisplayName("Emprunts en cours : un emprunt retourné n'est plus compté dans le quota")
    void testEmpruntRetourneLibereLeQuota() {
        LocalDate echeance = LocalDate.now().plusDays(14);

        Emprunt e1 = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), echeance);
        service.enregistrerEmprunt(livre2.getId(), membre1.getId(), echeance);
        service.enregistrerEmprunt(livre3.getId(), membre1.getId(), echeance);

        // Le membre a atteint son quota
        assertThrows(QuotaEmpruntDepasseException.class,
                () -> service.enregistrerEmprunt(livre4.getId(), membre1.getId(), echeance));

        // Le membre retourne le premier livre
        service.enregistrerRetour(e1.getId());

        // Le quota est libéré, le 4ème livre peut être emprunté
        Emprunt e4 = service.enregistrerEmprunt(livre4.getId(), membre1.getId(), echeance);
        assertNotNull(e4);
        assertEquals(3, empruntRepository.findEnCoursByMembreId(membre1.getId()).size());
    }

    @Test
    @DisplayName("Emprunts en retard : filtrage correct selon date et statut de retour")
    void testFindEmpruntsEnRetard() {
        LocalDate today = LocalDate.now();

        // 1. Emprunt en retard (date emprunt il y a 20j, date retour prévue il y a 5j, non retourné)
        Emprunt empruntEnRetard = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), today.minusDays(20), today.minusDays(5));

        // 2. Emprunt en cours non en retard (date retour prévue dans 7j)
        service.enregistrerEmprunt(livre2.getId(), membre1.getId(), today, today.plusDays(7));

        // 3. Emprunt retourné qui était auparavant en retard
        Emprunt empruntPasseEnRetardMaisRetourne = service.enregistrerEmprunt(livre3.getId(), membre2.getId(), today.minusDays(20), today.minusDays(5));
        service.enregistrerRetour(empruntPasseEnRetardMaisRetourne.getId(), today.minusDays(2));

        List<Emprunt> enRetard = service.findEmpruntsEnRetard();

        assertEquals(1, enRetard.size());
        assertEquals(empruntEnRetard.getId(), enRetard.get(0).getId());
    }

    @Test
    @DisplayName("Membre inexistant : refus de l'emprunt")
    void testMembreInexistant() {
        BibliothequeException ex = assertThrows(
                BibliothequeException.class,
                () -> service.enregistrerEmprunt(livre1.getId(), 999L, LocalDate.now().plusDays(7))
        );
        assertTrue(ex.getMessage().contains("Membre introuvable"));
    }

    @Test
    @DisplayName("Livre inexistant : refus de l'emprunt")
    void testLivreInexistant() {
        BibliothequeException ex = assertThrows(
                BibliothequeException.class,
                () -> service.enregistrerEmprunt(999L, membre1.getId(), LocalDate.now().plusDays(7))
        );
        assertTrue(ex.getMessage().contains("Livre introuvable"));
    }

    @Test
    @DisplayName("Retour avec emprunt inexistant : exception levée")
    void testRetourEmpruntInexistant() {
        BibliothequeException ex = assertThrows(
                BibliothequeException.class,
                () -> service.enregistrerRetour(999L)
        );
        assertTrue(ex.getMessage().contains("Emprunt introuvable"));
    }

    @Test
    @DisplayName("Listes des emprunts d'un membre : tous et en cours")
    void testListesEmpruntsMembre() {
        LocalDate echeance = LocalDate.now().plusDays(10);
        Emprunt e1 = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), echeance);
        Emprunt e2 = service.enregistrerEmprunt(livre2.getId(), membre1.getId(), echeance);

        service.enregistrerRetour(e1.getId());

        List<Emprunt> tous = service.findEmpruntsByMembre(membre1.getId());
        List<Emprunt> enCours = service.findEmpruntsEnCoursByMembre(membre1.getId());

        assertEquals(2, tous.size());
        assertEquals(1, enCours.size());
        assertEquals(e2.getId(), enCours.get(0).getId());

        assertThrows(BibliothequeException.class, () -> service.findEmpruntsByMembre(999L));
        assertThrows(BibliothequeException.class, () -> service.findEmpruntsEnCoursByMembre(999L));
    }

    @Test
    @DisplayName("Arguments null dans les méthodes du service lèvent IllegalArgumentException")
    void testArgumentsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerEmprunt(null, membre1.getId(), LocalDate.now().plusDays(7)));
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerEmprunt(livre1.getId(), null, LocalDate.now().plusDays(7)));
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerEmprunt(livre1.getId(), membre1.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> service.enregistrerRetour(null));
        assertThrows(IllegalArgumentException.class, () -> service.findEmpruntsByMembre(null));
        assertThrows(IllegalArgumentException.class, () -> service.findEmpruntsByMembre(1L, null));
        assertThrows(IllegalArgumentException.class, () -> service.findEmpruntsByMembreTriesParDateRetourPrevue(null));
        assertThrows(IllegalArgumentException.class, () -> service.findEmpruntsEnCoursByMembre(null));
    }

    @Test
    @DisplayName("Tri Comparator : les emprunts d'un membre sont ordonnés selon la date de retour prévue")
    void testTriEmpruntsParDateRetourPrevue() {
        LocalDate today = LocalDate.now();
        // Emprunt 1 : échéance dans 20 jours
        Emprunt e1 = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), today, today.plusDays(20));
        // Emprunt 2 : échéance dans 5 jours (doit arriver en 1er)
        Emprunt e2 = service.enregistrerEmprunt(livre2.getId(), membre1.getId(), today, today.plusDays(5));
        // Emprunt 3 : échéance dans 12 jours (doit arriver en 2ème)
        Emprunt e3 = service.enregistrerEmprunt(livre3.getId(), membre1.getId(), today, today.plusDays(12));

        List<Emprunt> triees = service.findEmpruntsByMembreTriesParDateRetourPrevue(membre1.getId());

        assertEquals(3, triees.size());
        assertEquals(e2.getId(), triees.get(0).getId(), "Le 1er doit être celui qui a l'échéance à +5 jours");
        assertEquals(e3.getId(), triees.get(1).getId(), "Le 2ème doit être celui qui a l'échéance à +12 jours");
        assertEquals(e1.getId(), triees.get(2).getId(), "Le 3ème doit être celui qui a l'échéance à +20 jours");

        assertTrue(triees.get(0).getDateRetourPrevue().isBefore(triees.get(1).getDateRetourPrevue()));
        assertTrue(triees.get(1).getDateRetourPrevue().isBefore(triees.get(2).getDateRetourPrevue()));
    }

    @Test
    @DisplayName("Tri Comparator : dates de retour prévues identiques gérées correctement")
    void testTriEmpruntsDatesIdentiques() {
        LocalDate sameDate = LocalDate.now().plusDays(10);
        Emprunt e1 = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), LocalDate.now(), sameDate);
        Emprunt e2 = service.enregistrerEmprunt(livre2.getId(), membre1.getId(), LocalDate.now(), sameDate);

        List<Emprunt> triees = service.findEmpruntsByMembreTriesParDateRetourPrevue(membre1.getId());

        assertEquals(2, triees.size());
        assertEquals(sameDate, triees.get(0).getDateRetourPrevue());
        assertEquals(sameDate, triees.get(1).getDateRetourPrevue());
    }

    @Test
    @DisplayName("Tri Comparator : membre sans emprunt retourne une liste vide")
    void testTriEmpruntsMembreSansEmprunt() {
        List<Emprunt> triees = service.findEmpruntsByMembreTriesParDateRetourPrevue(membre1.getId());
        assertNotNull(triees);
        assertTrue(triees.isEmpty());
    }

    @Test
    @DisplayName("Tri Comparator : membre inexistant lève BibliothequeException")
    void testTriEmpruntsMembreInexistant() {
        BibliothequeException ex = assertThrows(
                BibliothequeException.class,
                () -> service.findEmpruntsByMembreTriesParDateRetourPrevue(999L)
        );
        assertTrue(ex.getMessage().contains("Membre introuvable"));
    }

    @Test
    @DisplayName("Tri Comparator : utilisation d'un comparateur personnalisé (ordre inverse)")
    void testTriEmpruntsAvecComparateurPersonnalise() {
        LocalDate today = LocalDate.now();
        Emprunt e1 = service.enregistrerEmprunt(livre1.getId(), membre1.getId(), today, today.plusDays(10));
        Emprunt e2 = service.enregistrerEmprunt(livre2.getId(), membre1.getId(), today, today.plusDays(30));

        Comparator<Emprunt> comparateurInverse = Comparator.comparing(Emprunt::getDateRetourPrevue).reversed();
        List<Emprunt> triees = service.findEmpruntsByMembre(membre1.getId(), comparateurInverse);

        assertEquals(2, triees.size());
        assertEquals(e2.getId(), triees.get(0).getId(), "L'échéance la plus lointaine doit être première");
        assertEquals(e1.getId(), triees.get(1).getId());
    }
}
