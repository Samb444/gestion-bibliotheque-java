package sn.codesamb.service;

import sn.codesamb.model.CategorieLivre;
import sn.codesamb.model.Emprunt;
import sn.codesamb.repository.EmpruntRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service dédié au calcul et à l'agrégation de statistiques sur les emprunts de la bibliothèque.
 * <p>
 * Ce service s'appuie sur l'API {@link java.util.stream.Stream} pour analyser les données
 * d'emprunts et fournir des métriques métier (regroupements, décomptes par catégorie, etc.).
 */
public class StatistiqueBibliothequeService {

    private final EmpruntRepository empruntRepository;

    /**
     * Initialise le service statistique avec le repository d'emprunts requis.
     *
     * @param empruntRepository le repository des emprunts
     */
    public StatistiqueBibliothequeService(EmpruntRepository empruntRepository) {
        this.empruntRepository = Objects.requireNonNull(empruntRepository, "L'empruntRepository est obligatoire.");
    }

    /**
     * Calcule le nombre total d'emprunts par catégorie de livre pour l'ensemble des emprunts enregistrés.
     * <p>
     * Utilise l'API Stream avec {@link Collectors#groupingBy} et {@link Collectors#counting()}.
     * Les catégories n'ayant aucun emprunt ne sont pas incluses dans la Map résultante.
     *
     * @return une Map associant chaque catégorie présente au nombre d'emprunts correspondants
     */
    public Map<CategorieLivre, Long> compterEmpruntsParCategorie() {
        return compterEmpruntsParCategorie(empruntRepository.findAll());
    }

    /**
     * Calcule le nombre d'emprunts par catégorie de livre à partir d'une liste explicite d'emprunts.
     * <p>
     * Utilise l'API Stream avec {@link Collectors#groupingBy} et {@link Collectors#counting()}.
     *
     * @param emprunts la liste des emprunts à analyser
     * @return une Map associant chaque catégorie de livre au nombre d'emprunts correspondants
     * @throws IllegalArgumentException si la liste fournie est nulle
     */
    public Map<CategorieLivre, Long> compterEmpruntsParCategorie(List<Emprunt> emprunts) {
        if (emprunts == null) {
            throw new IllegalArgumentException("La liste des emprunts ne peut pas être nulle.");
        }
        return emprunts.stream()
                .collect(Collectors.groupingBy(
                        emprunt -> emprunt.getLivre().getCategorie(),
                        Collectors.counting()
                ));
    }

    /**
     * Calcule le nombre d'emprunts actuellement en cours (non retournés) par catégorie de livre.
     *
     * @return une Map associant chaque catégorie au nombre d'emprunts en cours
     */
    public Map<CategorieLivre, Long> compterEmpruntsEnCoursParCategorie() {
        return empruntRepository.findAll().stream()
                .filter(Emprunt::estEnCours)
                .collect(Collectors.groupingBy(
                        emprunt -> emprunt.getLivre().getCategorie(),
                        Collectors.counting()
                ));
    }

    /**
     * Retourne les statistiques d'emprunts par catégorie sous forme de liste d'entrées
     * triées par nombre d'emprunts décroissant, puis par nom de catégorie en cas d'égalité.
     *
     * @return la liste des statistiques triées par volume d'emprunts décroissant
     */
    public List<Map.Entry<CategorieLivre, Long>> getStatistiquesTrieesParNombreDecroissant() {
        return compterEmpruntsParCategorie().entrySet().stream()
                .sorted(Map.Entry.<CategorieLivre, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(entry -> entry.getKey().name()))
                .toList();
    }

    /**
     * Génère une représentation textuelle standard des statistiques d'emprunts par catégorie.
     * <p>
     * Exemple de format produit :
     * <pre>
     * INFORMATIQUE : 3
     * ROMAN : 2
     * HISTOIRE : 1
     * </pre>
     *
     * @return une chaîne formatée affichant chaque catégorie et son nombre d'emprunts
     */
    public String formaterStatistiquesParCategorie() {
        return getStatistiquesTrieesParNombreDecroissant().stream()
                .map(entry -> entry.getKey().name() + " : " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }
}
