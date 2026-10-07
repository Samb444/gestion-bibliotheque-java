package sn.codesamb.gestionsallesport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("Test de chargement du contexte Spring Boot")
class GestionSalleSportApplicationTests {

    @Test
    @DisplayName("Le contexte applicatif doit se charger sans erreur")
    void contextLoads() {
        // Vérifie que la configuration globale et les beans Spring s'initialisent correctement
    }
}
