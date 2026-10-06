package sn.codesamb.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    @DisplayName("Livre doit être disponible par défaut et changer d'état correctement")
    void testLivreEtatEtDisponibilite() {
        Livre livre = new Livre(1L, "Les Misérables", "Victor Hugo", CategorieLivre.ROMAN);

        assertEquals(1L, livre.getId());
        assertEquals("Les Misérables", livre.getTitre());
        assertEquals("Victor Hugo", livre.getAuteur());
        assertEquals(CategorieLivre.ROMAN, livre.getCategorie());
        assertEquals(EtatLivre.DISPONIBLE, livre.getEtat());
        assertTrue(livre.estDisponible());

        livre.setEtat(EtatLivre.EMPRUNTE);
        assertFalse(livre.estDisponible());
        assertEquals(EtatLivre.EMPRUNTE, livre.getEtat());
    }

    @Test
    @DisplayName("Livre doit lever des exceptions si les données obligatoires sont manquantes")
    void testLivreValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Livre(1L, "", "Auteur", CategorieLivre.ROMAN));
        assertThrows(IllegalArgumentException.class, () -> new Livre(1L, "Titre", "   ", CategorieLivre.ROMAN));
        assertThrows(NullPointerException.class, () -> new Livre(1L, "Titre", "Auteur", null));
    }

    @Test
    @DisplayName("Membre valide et vérification de ses propriétés")
    void testMembre() {
        Membre membre = new Membre(1L, "Diop", "Amadou", "amadou.diop@example.com");

        assertEquals(1L, membre.getId());
        assertEquals("Diop", membre.getNom());
        assertEquals("Amadou", membre.getPrenom());
        assertEquals("amadou.diop@example.com", membre.getEmail());

        membre.setEmail("a.diop@example.com");
        assertEquals("a.diop@example.com", membre.getEmail());

        assertThrows(IllegalArgumentException.class, () -> new Membre(2L, "", "Prenom", "email"));
        assertThrows(IllegalArgumentException.class, () -> new Membre(2L, "Nom", "  ", "email"));
        assertThrows(IllegalArgumentException.class, () -> new Membre(2L, "Nom", "Prenom", " "));
    }

    @Test
    @DisplayName("Emprunt en cours et gestion du retard")
    void testEmpruntEnCoursEtRetard() {
        Livre livre = new Livre(1L, "Clean Code", "Robert C. Martin", CategorieLivre.INFORMATIQUE);
        Membre membre = new Membre(1L, "Sow", "Fatou", "fatou.sow@example.com");

        LocalDate today = LocalDate.now();
        LocalDate pastDate = today.minusDays(10);
        LocalDate expiredPrevue = today.minusDays(2);
        LocalDate futurePrevue = today.plusDays(7);

        // Emprunt en cours non en retard
        Emprunt empruntActif = new Emprunt(1L, livre, membre, today, futurePrevue);
        assertTrue(empruntActif.estEnCours());
        assertFalse(empruntActif.estEnRetard());

        // Emprunt en cours en retard (date prévue dépassée)
        Emprunt empruntEnRetard = new Emprunt(2L, livre, membre, pastDate, expiredPrevue);
        assertTrue(empruntEnRetard.estEnCours());
        assertTrue(empruntEnRetard.estEnRetard());

        // Emprunt retourné dans les temps
        empruntEnRetard.setDateRetourEffective(expiredPrevue.minusDays(1));
        assertFalse(empruntEnRetard.estEnCours());
        assertFalse(empruntEnRetard.estEnRetard());

        // Emprunt retourné avec retard
        empruntEnRetard.setDateRetourEffective(expiredPrevue.plusDays(1));
        assertFalse(empruntEnRetard.estEnCours());
        assertTrue(empruntEnRetard.estEnRetard());
    }

    @Test
    @DisplayName("Emprunt validation des dates et champs obligatoires")
    void testEmpruntValidations() {
        Livre livre = new Livre(1L, "1984", "George Orwell", CategorieLivre.ROMAN);
        Membre membre = new Membre(1L, "Ndiaye", "Moussa", "moussa@example.com");
        LocalDate today = LocalDate.now();

        assertThrows(NullPointerException.class, () -> new Emprunt(1L, null, membre, today, today.plusDays(7)));
        assertThrows(NullPointerException.class, () -> new Emprunt(1L, livre, null, today, today.plusDays(7)));
        assertThrows(IllegalArgumentException.class, () -> new Emprunt(1L, livre, membre, today, today.minusDays(1)));
    }
}
