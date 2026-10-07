package sn.codesamb.gestionsallesport.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.codesamb.gestionsallesport.entity.Cours;
import sn.codesamb.gestionsallesport.entity.TypeCours;
import sn.codesamb.gestionsallesport.repository.CoursRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service {@link CoursService} isolés avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - CoursService")
class CoursServiceTest {

    @Mock
    private CoursRepository coursRepository;

    @InjectMocks
    private CoursService coursService;

    private Cours createSampleCours(Long id, String nom, TypeCours type) {
        Cours cours = new Cours(
                nom,
                type,
                25,
                LocalDateTime.of(2026, 10, 15, 10, 0),
                "Salle B"
        );
        cours.setId(id);
        return cours;
    }

    @Test
    @DisplayName("findAll() doit déléguer à coursRepository.findAll() et retourner la liste")
    void findAll_ShouldDelegateToRepository() {
        // Given
        Cours c1 = createSampleCours(1L, "Yoga Débutant", TypeCours.YOGA);
        Cours c2 = createSampleCours(2L, "Cardio Training", TypeCours.CARDIO);
        List<Cours> coursList = List.of(c1, c2);
        when(coursRepository.findAll()).thenReturn(coursList);

        // When
        List<Cours> result = coursService.findAll();

        // Then
        assertThat(result).hasSize(2).containsExactly(c1, c2);
        verify(coursRepository).findAll();
    }

    @Test
    @DisplayName("findById() doit déléguer à coursRepository.findById() quand le cours existe")
    void findById_WhenExists_ShouldReturnCours() {
        // Given
        Cours cours = createSampleCours(1L, "HIIT Express", TypeCours.HIIT);
        when(coursRepository.findById(1L)).thenReturn(Optional.of(cours));

        // When
        Optional<Cours> result = coursService.findById(1L);

        // Then
        assertThat(result).isPresent().contains(cours);
        verify(coursRepository).findById(1L);
    }

    @Test
    @DisplayName("findById() doit retourner Optional.empty() quand le cours n'existe pas")
    void findById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(coursRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Cours> result = coursService.findById(99L);

        // Then
        assertThat(result).isEmpty();
        verify(coursRepository).findById(99L);
    }

    @Test
    @DisplayName("save() doit déléguer à coursRepository.save() et retourner le cours persisté")
    void save_ShouldDelegateToRepository() {
        // Given
        Cours coursToSave = createSampleCours(null, "Musculation Force", TypeCours.MUSCULATION);
        Cours savedCours = createSampleCours(1L, "Musculation Force", TypeCours.MUSCULATION);
        when(coursRepository.save(coursToSave)).thenReturn(savedCours);

        // When
        Cours result = coursService.save(coursToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNom()).isEqualTo("Musculation Force");
        verify(coursRepository).save(coursToSave);
    }

    @Test
    @DisplayName("deleteById() doit déléguer à coursRepository.deleteById()")
    void deleteById_ShouldDelegateToRepository() {
        // Given
        Long id = 1L;

        // When
        coursService.deleteById(id);

        // Then
        verify(coursRepository).deleteById(id);
    }
}
