package sn.codesamb.gestionsallesport.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.codesamb.gestionsallesport.entity.Adherent;
import sn.codesamb.gestionsallesport.repository.AdherentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service {@link AdherentService} isolés avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AdherentService")
class AdherentServiceTest {

    @Mock
    private AdherentRepository adherentRepository;

    @InjectMocks
    private AdherentService adherentService;

    private Adherent createSampleAdherent(Long id, String email) {
        Adherent adherent = new Adherent(
                "Ndiaye",
                "Fatou",
                email,
                "+221770000000",
                LocalDate.of(1995, 5, 20)
        );
        adherent.setId(id);
        return adherent;
    }

    @Test
    @DisplayName("findAll() doit déléguer à adherentRepository.findAll() et retourner la liste")
    void findAll_ShouldDelegateToRepository() {
        // Given
        Adherent a1 = createSampleAdherent(1L, "fatou1@example.com");
        Adherent a2 = createSampleAdherent(2L, "fatou2@example.com");
        List<Adherent> adherents = List.of(a1, a2);
        when(adherentRepository.findAll()).thenReturn(adherents);

        // When
        List<Adherent> result = adherentService.findAll();

        // Then
        assertThat(result).hasSize(2).containsExactly(a1, a2);
        verify(adherentRepository).findAll();
    }

    @Test
    @DisplayName("findById() doit déléguer à adherentRepository.findById() quand l'adhérent existe")
    void findById_WhenExists_ShouldReturnAdherent() {
        // Given
        Adherent adherent = createSampleAdherent(1L, "fatou@example.com");
        when(adherentRepository.findById(1L)).thenReturn(Optional.of(adherent));

        // When
        Optional<Adherent> result = adherentService.findById(1L);

        // Then
        assertThat(result).isPresent().contains(adherent);
        verify(adherentRepository).findById(1L);
    }

    @Test
    @DisplayName("findById() doit retourner Optional.empty() quand l'adhérent n'existe pas")
    void findById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(adherentRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Adherent> result = adherentService.findById(99L);

        // Then
        assertThat(result).isEmpty();
        verify(adherentRepository).findById(99L);
    }

    @Test
    @DisplayName("findByEmail() doit déléguer à adherentRepository.findByEmail() quand l'email existe")
    void findByEmail_WhenExists_ShouldReturnAdherent() {
        // Given
        String email = "fatou@example.com";
        Adherent adherent = createSampleAdherent(1L, email);
        when(adherentRepository.findByEmail(email)).thenReturn(Optional.of(adherent));

        // When
        Optional<Adherent> result = adherentService.findByEmail(email);

        // Then
        assertThat(result).isPresent().contains(adherent);
        verify(adherentRepository).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail() doit retourner Optional.empty() quand l'email n'existe pas")
    void findByEmail_WhenNotExists_ShouldReturnEmpty() {
        // Given
        String email = "inconnu@example.com";
        when(adherentRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<Adherent> result = adherentService.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(adherentRepository).findByEmail(email);
    }

    @Test
    @DisplayName("save() doit déléguer à adherentRepository.save() et retourner l'adhérent persisté")
    void save_ShouldDelegateToRepository() {
        // Given
        Adherent adherentToSave = createSampleAdherent(null, "nouveau@example.com");
        Adherent savedAdherent = createSampleAdherent(1L, "nouveau@example.com");
        when(adherentRepository.save(adherentToSave)).thenReturn(savedAdherent);

        // When
        Adherent result = adherentService.save(adherentToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("nouveau@example.com");
        verify(adherentRepository).save(adherentToSave);
    }

    @Test
    @DisplayName("deleteById() doit déléguer à adherentRepository.deleteById()")
    void deleteById_ShouldDelegateToRepository() {
        // Given
        Long id = 1L;

        // When
        adherentService.deleteById(id);

        // Then
        verify(adherentRepository).deleteById(id);
    }
}
