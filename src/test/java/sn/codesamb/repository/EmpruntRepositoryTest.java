package sn.codesamb.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.codesamb.model.CategorieLivre;
import sn.codesamb.model.Emprunt;
import sn.codesamb.model.Livre;
import sn.codesamb.model.Membre;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmpruntRepositoryTest {

    private EmpruntRepository repository;
    private Livre livre1;
    private Livre livre2;
    private Membre membre1;
    private Membre membre2;

    @BeforeEach
    void setUp() {
        repository = new EmpruntRepository();
        livre1 = new Livre(1L, "Les Misérables", "Victor Hugo", CategorieLivre.ROMAN);
        livre2 = new Livre(2L, "Clean Code", "Robert C. Martin", CategorieLivre.INFORMATIQUE);
        membre1 = new Membre(1L, "Diop", "Amadou", "amadou@example.com");
        membre2 = new Membre(2L, "Fall", "Awa", "awa@example.com");
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un emprunt par identifiant")
    void testSauvegardeEtRechercheEmprunt() {
        LocalDate today = LocalDate.now();
        Emprunt emprunt = new Emprunt(1L, livre1, membre1, today, today.plusDays(14));

        repository.save(emprunt);

        assertTrue(repository.existsById(1L));
        Optional<Emprunt> found = repository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals(livre1, found.get().getLivre());
        assertEquals(membre1, found.get().getMembre());
    }

    @Test
    @DisplayName("Génération automatique d'identifiants séquentiels")
    void testNextId() {
        assertEquals(1L, repository.nextId());

        Emprunt e1 = new Emprunt(1L, livre1, membre1, LocalDate.now(), LocalDate.now().plusDays(7));
        repository.save(e1);

        assertEquals(2L, repository.nextId());
    }

    @Test
    @DisplayName("Trouver tous les emprunts d'un membre")
    void testFindByMembreId() {
        LocalDate today = LocalDate.now();
        Emprunt e1 = new Emprunt(1L, livre1, membre1, today, today.plusDays(7));
        Emprunt e2 = new Emprunt(2L, livre2, membre1, today, today.plusDays(7));
        Emprunt e3 = new Emprunt(3L, livre1, membre2, today, today.plusDays(7));

        repository.save(e1);
        repository.save(e2);
        repository.save(e3);

        List<Emprunt> empruntsMembre1 = repository.findByMembreId(1L);
        assertEquals(2, empruntsMembre1.size());
        assertTrue(empruntsMembre1.contains(e1));
        assertTrue(empruntsMembre1.contains(e2));

        List<Emprunt> empruntsMembre2 = repository.findByMembreId(2L);
        assertEquals(1, empruntsMembre2.size());
        assertTrue(empruntsMembre2.contains(e3));

        assertTrue(repository.findByMembreId(999L).isEmpty());
        assertTrue(repository.findByMembreId(null).isEmpty());
    }

    @Test
    @DisplayName("Trouver les emprunts en cours d'un membre")
    void testFindEnCoursByMembreId() {
        LocalDate today = LocalDate.now();
        Emprunt e1 = new Emprunt(1L, livre1, membre1, today, today.plusDays(7));
        Emprunt e2 = new Emprunt(2L, livre2, membre1, today, today.plusDays(7));

        e2.setDateRetourEffective(today.plusDays(2)); // retourné

        repository.save(e1);
        repository.save(e2);

        List<Emprunt> enCours = repository.findEnCoursByMembreId(1L);
        assertEquals(1, enCours.size());
        assertEquals(1L, enCours.get(0).getId());
    }

    @Test
    @DisplayName("Trouver les emprunts en retard")
    void testFindEnRetard() {
        LocalDate today = LocalDate.now();
        // Emprunt en retard (date retour prévue hier, toujours en cours)
        Emprunt e1 = new Emprunt(1L, livre1, membre1, today.minusDays(10), today.minusDays(1));
        // Emprunt dans les temps (non en retard)
        Emprunt e2 = new Emprunt(2L, livre2, membre1, today, today.plusDays(7));
        // Emprunt retourné même si la date prévue était dépassée
        Emprunt e3 = new Emprunt(3L, livre1, membre2, today.minusDays(10), today.minusDays(1), today.minusDays(2));

        repository.save(e1);
        repository.save(e2);
        repository.save(e3);

        List<Emprunt> enRetard = repository.findEnRetard();
        assertEquals(1, enRetard.size());
        assertEquals(1L, enRetard.get(0).getId());
    }

    @Test
    @DisplayName("Suppression d'un emprunt")
    void testSuppressionEmprunt() {
        LocalDate today = LocalDate.now();
        Emprunt e1 = new Emprunt(10L, livre1, membre1, today, today.plusDays(7));
        repository.save(e1);

        assertTrue(repository.deleteById(10L));
        assertFalse(repository.existsById(10L));
        assertEquals(0, repository.count());
    }
}
