package sn.codesamb.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sn.codesamb.model.CategorieLivre;
import sn.codesamb.model.EtatLivre;
import sn.codesamb.model.Livre;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LivreRepositoryTest {

    private LivreRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LivreRepository();
    }

    @Test
    @DisplayName("Sauvegarder et retrouver un Livre par son identifiant")
    void testSauvegardeEtRechercheLivre() {
        Livre livre = new Livre(1L, "Le Petit Prince", "Antoine de Saint-Exupéry", CategorieLivre.ROMAN);
        repository.save(livre);

        assertTrue(repository.existsById(1L));
        Optional<Livre> found = repository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("Le Petit Prince", found.get().getTitre());
        assertEquals(CategorieLivre.ROMAN, found.get().getCategorie());
    }

    @Test
    @DisplayName("Mettre à jour l'état d'un livre existant")
    void testMiseAJourEtatLivre() {
        Livre livre = new Livre(1L, "Clean Architecture", "Robert C. Martin", CategorieLivre.INFORMATIQUE);
        repository.save(livre);

        livre.setEtat(EtatLivre.EMPRUNTE);
        repository.save(livre);

        assertEquals(1, repository.count());
        Optional<Livre> found = repository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals(EtatLivre.EMPRUNTE, found.get().getEtat());
    }

    @Test
    @DisplayName("Récupérer tous les livres")
    void testFindAllLivres() {
        Livre l1 = new Livre(1L, "Livre 1", "Auteur 1", CategorieLivre.DROIT);
        Livre l2 = new Livre(2L, "Livre 2", "Auteur 2", CategorieLivre.HISTOIRE);

        repository.save(l1);
        repository.save(l2);

        List<Livre> livres = repository.findAll();
        assertEquals(2, livres.size());
        assertTrue(livres.contains(l1));
        assertTrue(livres.contains(l2));
    }

    @Test
    @DisplayName("Supprimer un livre")
    void testSuppressionLivre() {
        Livre livre = new Livre(10L, "Dune", "Frank Herbert", CategorieLivre.SCIENCE);
        repository.save(livre);

        assertTrue(repository.deleteById(10L));
        assertFalse(repository.existsById(10L));
        assertTrue(repository.findById(10L).isEmpty());
    }
}
