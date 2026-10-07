package sn.codesamb.gestionsallesport.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Tests d'intégration et de conformité du métamodèle JPA")
class JpaEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Le contexte JPA doit être initialisé et l'EntityManager injecté")
    void entityManagerShouldBePresent() {
        assertNotNull(entityManager, "L'EntityManager Spring Data JPA doit être disponible");
    }

    @Test
    @DisplayName("Les 3 entités doivent être correctement enregistrées dans le métamodèle JPA")
    void entitiesShouldBeRegisteredInMetamodel() {
        Metamodel metamodel = entityManager.getMetamodel();
        assertNotNull(metamodel, "Le métamodèle JPA ne doit pas être null");

        Set<String> entityNames = metamodel.getEntities().stream()
                .map(EntityType::getName)
                .collect(Collectors.toSet());

        assertTrue(entityNames.contains("Adherent"), "L'entité Adherent doit être détectée par JPA");
        assertTrue(entityNames.contains("Cours"), "L'entité Cours doit être détectée par JPA");
        assertTrue(entityNames.contains("Inscription"), "L'entité Inscription doit être détectée par JPA");
    }

    @Test
    @DisplayName("Vérification des attributs et relations de l'entité Adherent")
    void testAdherentMetamodelMapping() {
        EntityType<Adherent> adherentType = entityManager.getMetamodel().entity(Adherent.class);
        assertNotNull(adherentType);

        assertNotNull(adherentType.getAttribute("id"));
        assertNotNull(adherentType.getAttribute("nom"));
        assertNotNull(adherentType.getAttribute("prenom"));
        assertNotNull(adherentType.getAttribute("email"));
        assertNotNull(adherentType.getAttribute("telephone"));
        assertNotNull(adherentType.getAttribute("dateNaissance"));
        assertNotNull(adherentType.getAttribute("inscriptions"));

        assertTrue(adherentType.getAttribute("inscriptions").isCollection(),
                "Le champ inscriptions doit être une collection dans Adherent");
    }

    @Test
    @DisplayName("Vérification des attributs et relations de l'entité Cours")
    void testCoursMetamodelMapping() {
        EntityType<Cours> coursType = entityManager.getMetamodel().entity(Cours.class);
        assertNotNull(coursType);

        assertNotNull(coursType.getAttribute("id"));
        assertNotNull(coursType.getAttribute("nom"));
        assertNotNull(coursType.getAttribute("type"));
        assertNotNull(coursType.getAttribute("capacite"));
        assertNotNull(coursType.getAttribute("dateHeure"));
        assertNotNull(coursType.getAttribute("salle"));
        assertNotNull(coursType.getAttribute("inscriptions"));

        assertTrue(coursType.getAttribute("inscriptions").isCollection(),
                "Le champ inscriptions doit être une collection dans Cours");
    }

    @Test
    @DisplayName("Vérification des attributs et relations de l'entité Inscription")
    void testInscriptionMetamodelMapping() {
        EntityType<Inscription> inscriptionType = entityManager.getMetamodel().entity(Inscription.class);
        assertNotNull(inscriptionType);

        assertNotNull(inscriptionType.getAttribute("id"));
        assertNotNull(inscriptionType.getAttribute("dateInscription"));
        assertNotNull(inscriptionType.getAttribute("presence"));
        assertNotNull(inscriptionType.getAttribute("adherent"));
        assertNotNull(inscriptionType.getAttribute("cours"));

        assertTrue(inscriptionType.getAttribute("adherent").isAssociation(),
                "L'adhérent doit être une association ManyToOne dans Inscription");
        assertTrue(inscriptionType.getAttribute("cours").isAssociation(),
                "Le cours doit être une association ManyToOne dans Inscription");
    }
}
