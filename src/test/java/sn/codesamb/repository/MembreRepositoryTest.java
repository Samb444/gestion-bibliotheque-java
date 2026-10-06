package sn.codesamb.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.codesamb.model.Membre;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MembreRepositoryTest {

    private MembreRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MembreRepository();
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un Membre par son identifiant")
    void testSauvegardeEtRechercheMembre() {
        Membre membre = new Membre(1L, "Ba", "Aissatou", "aissatou.ba@example.com");
        repository.save(membre);

        assertTrue(repository.existsById(1L));
        Optional<Membre> found = repository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("Ba", found.get().getNom());
        assertEquals("Aissatou", found.get().getPrenom());
        assertEquals("aissatou.ba@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Mettre à jour l'email d'un membre")
    void testMiseAJourMembre() {
        Membre membre = new Membre(1L, "Ba", "Aissatou", "ancien@example.com");
        repository.save(membre);

        membre.setEmail("nouveau@example.com");
        repository.save(membre);

        assertEquals(1, repository.count());
        Optional<Membre> found = repository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("nouveau@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Récupérer tous les membres")
    void testFindAllMembres() {
        Membre m1 = new Membre(1L, "Fall", "Cheikh", "cheikh@example.com");
        Membre m2 = new Membre(2L, "Gueye", "Mariama", "mariama@example.com");

        repository.save(m1);
        repository.save(m2);

        List<Membre> membres = repository.findAll();
        assertEquals(2, membres.size());
        assertTrue(membres.contains(m1));
        assertTrue(membres.contains(m2));
    }

    @Test
    @DisplayName("Supprimer un membre")
    void testSuppressionMembre() {
        Membre membre = new Membre(5L, "Kane", "Oumar", "oumar@example.com");
        repository.save(membre);

        assertTrue(repository.deleteById(5L));
        assertFalse(repository.existsById(5L));
        assertTrue(repository.findById(5L).isEmpty());
    }
}
