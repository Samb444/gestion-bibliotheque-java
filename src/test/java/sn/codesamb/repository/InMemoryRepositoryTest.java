package sn.codesamb.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    // Simple entité de test locale
    record Article(Long id, String designation) {}

    private InMemoryRepository<Article> repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRepository<>(Article::id);
    }

    @Nested
    @DisplayName("Tests de sauvegarde et mise à jour")
    class SauvegardeTests {

        @Test
        @DisplayName("Sauvegarder une entité valide et la retrouver")
        void testSauvegardeEntiteValide() {
            Article article = new Article(1L, "Livre de poche");
            Article saved = repository.save(article);

            assertEquals(article, saved);
            assertTrue(repository.existsById(1L));
            assertEquals(1, repository.count());

            Optional<Article> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals("Livre de poche", found.get().designation());
        }

        @Test
        @DisplayName("Mettre à jour une entité existante avec le même identifiant")
        void testMiseAJourEntite() {
            repository.save(new Article(1L, "Version initiale"));
            repository.save(new Article(1L, "Version modifiée"));

            assertEquals(1, repository.count());
            Optional<Article> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals("Version modifiée", found.get().designation());
        }
    }

    @Nested
    @DisplayName("Tests de recherche")
    class RechercheTests {

        @Test
        @DisplayName("Rechercher une entité existante retourne un Optional rempli")
        void testRechercheExistante() {
            Article article = new Article(10L, "Stylo");
            repository.save(article);

            Optional<Article> result = repository.findById(10L);
            assertTrue(result.isPresent());
            assertEquals(article, result.get());
        }

        @Test
        @DisplayName("Rechercher un identifiant inexistant retourne Optional.empty()")
        void testRechercheInexistante() {
            Optional<Article> result = repository.findById(999L);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Rechercher un identifiant null retourne Optional.empty()")
        void testRechercheIdNull() {
            Optional<Article> result = repository.findById(null);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests de liste (findAll)")
    class ListeTests {

        @Test
        @DisplayName("findAll retourne une liste vide lorsqu'aucune entité n'est stockée")
        void testFindAllVide() {
            List<Article> all = repository.findAll();
            assertNotNull(all);
            assertTrue(all.isEmpty());
        }

        @Test
        @DisplayName("findAll retourne toutes les entités sauvegardées dans l'ordre d'insertion")
        void testFindAllAvecDonnees() {
            Article a1 = new Article(1L, "Premier");
            Article a2 = new Article(2L, "Deuxième");
            Article a3 = new Article(3L, "Troisième");

            repository.save(a1);
            repository.save(a2);
            repository.save(a3);

            List<Article> all = repository.findAll();
            assertEquals(3, all.size());
            assertEquals(List.of(a1, a2, a3), all);
        }
    }

    @Nested
    @DisplayName("Tests de suppression")
    class SuppressionTests {

        @Test
        @DisplayName("Supprimer une entité existante retourne true et la supprime")
        void testSuppressionExistante() {
            repository.save(new Article(1L, "Cahier"));
            assertTrue(repository.existsById(1L));

            boolean deleted = repository.deleteById(1L);
            assertTrue(deleted);
            assertFalse(repository.existsById(1L));
            assertTrue(repository.findById(1L).isEmpty());
            assertEquals(0, repository.count());
        }

        @Test
        @DisplayName("Supprimer un identifiant inexistant retourne false")
        void testSuppressionInexistante() {
            boolean deleted = repository.deleteById(404L);
            assertFalse(deleted);
        }

        @Test
        @DisplayName("Supprimer un identifiant null retourne false sans erreur")
        void testSuppressionIdNull() {
            boolean deleted = repository.deleteById(null);
            assertFalse(deleted);
        }
    }

    @Nested
    @DisplayName("Tests des cas limites et validations")
    class CasLimitesTests {

        @Test
        @DisplayName("Sauvegarder une entité null lève une IllegalArgumentException")
        void testSauvegardeEntiteNull() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.save(null)
            );
            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Sauvegarder une entité avec identifiant null lève une IllegalArgumentException")
        void testSauvegardeEntiteIdNull() {
            Article articleSansId = new Article(null, "Sans ID");
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> repository.save(articleSansId)
            );
            assertTrue(exception.getMessage().contains("identifiant"));
        }

        @Test
        @DisplayName("existsById avec null retourne false")
        void testExistsByIdNull() {
            assertFalse(repository.existsById(null));
        }
    }
}
