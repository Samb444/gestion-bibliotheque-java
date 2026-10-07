package sn.codesamb.gestionsallesport.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires des entités du domaine et de leur intégrité")
class EntityUnitTest {

    @Test
    @DisplayName("Adherent : getters, setters et constructeur")
    void testAdherentProperties() {
        LocalDate birthDate = LocalDate.of(2000, 5, 15);
        Adherent adherent = new Adherent("Diop", "Moussa", "moussa.diop@example.com", "+221771234567", birthDate);
        adherent.setId(10L);

        assertEquals(10L, adherent.getId());
        assertEquals("Diop", adherent.getNom());
        assertEquals("Moussa", adherent.getPrenom());
        assertEquals("moussa.diop@example.com", adherent.getEmail());
        assertEquals("+221771234567", adherent.getTelephone());
        assertEquals(birthDate, adherent.getDateNaissance());
        assertNotNull(adherent.getInscriptions());
        assertTrue(adherent.getInscriptions().isEmpty());
    }

    @Test
    @DisplayName("Cours : getters, setters, enums et validation de capacité")
    void testCoursProperties() {
        LocalDateTime sessionTime = LocalDateTime.of(2026, 11, 1, 10, 0);
        Cours cours = new Cours("CrossFit Matin", TypeCours.HIIT, 25, sessionTime, "Salle A");
        cours.setId(5L);

        assertEquals(5L, cours.getId());
        assertEquals("CrossFit Matin", cours.getNom());
        assertEquals(TypeCours.HIIT, cours.getType());
        assertEquals(25, cours.getCapacite());
        assertEquals(sessionTime, cours.getDateHeure());
        assertEquals("Salle A", cours.getSalle());
        assertNotNull(cours.getInscriptions());

        // Test de la contrainte capacité strictement positive
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cours.setCapacite(0));
        assertTrue(ex.getMessage().contains("strictement positif"));

        assertThrows(IllegalArgumentException.class, () -> cours.setCapacite(-5));
    }

    @Test
    @DisplayName("Inscription : association Adherent <-> Inscription <-> Cours")
    void testInscriptionAssociationAndBidirectionality() {
        Adherent adherent = new Adherent("Sow", "Amina", "amina.sow@example.com", "+221781234567", LocalDate.of(1998, 2, 10));
        adherent.setId(1L);

        Cours cours = new Cours("Yoga Doux", TypeCours.YOGA, 15, LocalDateTime.of(2026, 11, 2, 18, 30), "Studio Zen");
        cours.setId(2L);

        Inscription inscription = new Inscription(adherent, cours);
        inscription.setId(100L);
        inscription.setPresence(StatutPresence.PRESENT);

        // Association manuelle via helper methods
        adherent.addInscription(inscription);
        cours.addInscription(inscription);

        assertEquals(1, adherent.getInscriptions().size());
        assertEquals(1, cours.getInscriptions().size());
        assertSame(adherent, inscription.getAdherent());
        assertSame(cours, inscription.getCours());
        assertEquals(StatutPresence.PRESENT, inscription.getPresence());

        // Dissociation
        adherent.removeInscription(inscription);
        cours.removeInscription(inscription);
        assertTrue(adherent.getInscriptions().isEmpty());
        assertTrue(cours.getInscriptions().isEmpty());
        assertNull(inscription.getAdherent());
        assertNull(inscription.getCours());
    }

    @Test
    @DisplayName("Relations bidirectionnelles : absence de récursion infinie dans toString()")
    void testToStringNoInfiniteRecursion() {
        Adherent adherent = new Adherent("Ndiaye", "Fatou", "fatou@example.com", "+221761234567", LocalDate.of(1995, 8, 20));
        adherent.setId(1L);

        Cours cours = new Cours("Cardio Boxing", TypeCours.CARDIO, 20, LocalDateTime.of(2026, 11, 3, 19, 0), "Ring 1");
        cours.setId(2L);

        Inscription inscription = new Inscription(adherent, cours);
        inscription.setId(3L);
        adherent.addInscription(inscription);
        cours.addInscription(inscription);

        // Vérification qu'aucun StackOverflowError ne survient lors des appels toString()
        assertDoesNotThrow(() -> {
            String sAdherent = adherent.toString();
            String sCours = cours.toString();
            String sInscription = inscription.toString();

            assertNotNull(sAdherent);
            assertNotNull(sCours);
            assertNotNull(sInscription);
            assertTrue(sAdherent.contains("Adherent"));
            assertTrue(sCours.contains("Cours"));
            assertTrue(sInscription.contains("Inscription"));
        });
    }

    @Test
    @DisplayName("Égalité et hashCode cohérents")
    void testEqualsAndHashCode() {
        Adherent a1 = new Adherent("Diop", "Aliou", "aliou@example.com", "0102030405", LocalDate.of(1990, 1, 1));
        Adherent a2 = new Adherent("Diop", "Aliou", "aliou@example.com", "0102030405", LocalDate.of(1990, 1, 1));

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        a1.setId(1L);
        a2.setId(1L);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}
