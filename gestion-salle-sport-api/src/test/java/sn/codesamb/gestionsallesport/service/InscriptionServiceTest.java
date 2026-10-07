package sn.codesamb.gestionsallesport.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.codesamb.gestionsallesport.entity.Adherent;
import sn.codesamb.gestionsallesport.entity.Cours;
import sn.codesamb.gestionsallesport.entity.Inscription;
import sn.codesamb.gestionsallesport.entity.StatutPresence;
import sn.codesamb.gestionsallesport.entity.TypeCours;
import sn.codesamb.gestionsallesport.repository.InscriptionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service {@link InscriptionService} isolés avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - InscriptionService")
class InscriptionServiceTest {

    @Mock
    private InscriptionRepository inscriptionRepository;

    @InjectMocks
    private InscriptionService inscriptionService;

    private Adherent createSampleAdherent(Long id) {
        Adherent adherent = new Adherent(
                "Diop",
                "Moussa",
                "moussa@example.com",
                "+221771112233",
                LocalDate.of(1998, 3, 10)
        );
        adherent.setId(id);
        return adherent;
    }

    private Cours createSampleCours(Long id) {
        Cours cours = new Cours(
                "Cross Training",
                TypeCours.CIRCUIT,
                20,
                LocalDateTime.of(2026, 10, 16, 18, 0),
                "Salle Principale"
        );
        cours.setId(id);
        return cours;
    }

    private Inscription createSampleInscription(Long id, Adherent adherent, Cours cours) {
        Inscription inscription = new Inscription(
                adherent,
                cours,
                LocalDateTime.of(2026, 10, 10, 12, 0),
                StatutPresence.ABSENT
        );
        inscription.setId(id);
        return inscription;
    }

    @Test
    @DisplayName("findAll() doit déléguer à inscriptionRepository.findAll() et retourner la liste")
    void findAll_ShouldDelegateToRepository() {
        // Given
        Adherent adherent = createSampleAdherent(1L);
        Cours cours = createSampleCours(10L);
        Inscription ins1 = createSampleInscription(100L, adherent, cours);
        Inscription ins2 = createSampleInscription(101L, adherent, cours);
        when(inscriptionRepository.findAll()).thenReturn(List.of(ins1, ins2));

        // When
        List<Inscription> result = inscriptionService.findAll();

        // Then
        assertThat(result).hasSize(2).containsExactly(ins1, ins2);
        verify(inscriptionRepository).findAll();
    }

    @Test
    @DisplayName("findById() doit déléguer à inscriptionRepository.findById() quand l'inscription existe")
    void findById_WhenExists_ShouldReturnInscription() {
        // Given
        Adherent adherent = createSampleAdherent(1L);
        Cours cours = createSampleCours(10L);
        Inscription inscription = createSampleInscription(100L, adherent, cours);
        when(inscriptionRepository.findById(100L)).thenReturn(Optional.of(inscription));

        // When
        Optional<Inscription> result = inscriptionService.findById(100L);

        // Then
        assertThat(result).isPresent().contains(inscription);
        verify(inscriptionRepository).findById(100L);
    }

    @Test
    @DisplayName("findById() doit retourner Optional.empty() quand l'inscription n'existe pas")
    void findById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(inscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Inscription> result = inscriptionService.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(inscriptionRepository).findById(999L);
    }

    @Test
    @DisplayName("findByAdherentId() doit déléguer à inscriptionRepository.findByAdherentId()")
    void findByAdherentId_ShouldDelegateToRepository() {
        // Given
        Adherent adherent = createSampleAdherent(1L);
        Cours cours = createSampleCours(10L);
        Inscription inscription = createSampleInscription(100L, adherent, cours);
        when(inscriptionRepository.findByAdherentId(1L)).thenReturn(List.of(inscription));

        // When
        List<Inscription> result = inscriptionService.findByAdherentId(1L);

        // Then
        assertThat(result).hasSize(1).containsExactly(inscription);
        verify(inscriptionRepository).findByAdherentId(1L);
    }

    @Test
    @DisplayName("findByCoursId() doit déléguer à inscriptionRepository.findByCoursId()")
    void findByCoursId_ShouldDelegateToRepository() {
        // Given
        Adherent adherent = createSampleAdherent(1L);
        Cours cours = createSampleCours(10L);
        Inscription inscription = createSampleInscription(100L, adherent, cours);
        when(inscriptionRepository.findByCoursId(10L)).thenReturn(List.of(inscription));

        // When
        List<Inscription> result = inscriptionService.findByCoursId(10L);

        // Then
        assertThat(result).hasSize(1).containsExactly(inscription);
        verify(inscriptionRepository).findByCoursId(10L);
    }

    @Test
    @DisplayName("existsByAdherentIdAndCoursId() doit retourner true quand l'inscription existe")
    void existsByAdherentIdAndCoursId_WhenExists_ShouldReturnTrue() {
        // Given
        when(inscriptionRepository.existsByAdherentIdAndCoursId(1L, 10L)).thenReturn(true);

        // When
        boolean exists = inscriptionService.existsByAdherentIdAndCoursId(1L, 10L);

        // Then
        assertThat(exists).isTrue();
        verify(inscriptionRepository).existsByAdherentIdAndCoursId(1L, 10L);
    }

    @Test
    @DisplayName("existsByAdherentIdAndCoursId() doit retourner false quand l'inscription n'existe pas")
    void existsByAdherentIdAndCoursId_WhenNotExists_ShouldReturnFalse() {
        // Given
        when(inscriptionRepository.existsByAdherentIdAndCoursId(1L, 10L)).thenReturn(false);

        // When
        boolean exists = inscriptionService.existsByAdherentIdAndCoursId(1L, 10L);

        // Then
        assertThat(exists).isFalse();
        verify(inscriptionRepository).existsByAdherentIdAndCoursId(1L, 10L);
    }

    @Test
    @DisplayName("save() doit déléguer à inscriptionRepository.save() et retourner l'inscription persistée")
    void save_ShouldDelegateToRepository() {
        // Given
        Adherent adherent = createSampleAdherent(1L);
        Cours cours = createSampleCours(10L);
        Inscription toSave = createSampleInscription(null, adherent, cours);
        Inscription saved = createSampleInscription(100L, adherent, cours);
        when(inscriptionRepository.save(toSave)).thenReturn(saved);

        // When
        Inscription result = inscriptionService.save(toSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        verify(inscriptionRepository).save(toSave);
    }

    @Test
    @DisplayName("deleteById() doit déléguer à inscriptionRepository.deleteById()")
    void deleteById_ShouldDelegateToRepository() {
        // Given
        Long id = 100L;

        // When
        inscriptionService.deleteById(id);

        // Then
        verify(inscriptionRepository).deleteById(id);
    }
}
