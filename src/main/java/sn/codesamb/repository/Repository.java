package sn.codesamb.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface générique de persistance (CRUD de base) pour les entités.
 * <p>
 * Toutes les entités sont identifiées par une clé de type {@link Long}.
 *
 * @param <T> le type de l'entité manipulée
 */
public interface Repository<T> {

    /**
     * Sauvegarde une entité.
     * Si une entité avec le même identifiant existe déjà, elle est mise à jour (remplacée).
     *
     * @param entity l'entité à sauvegarder
     * @return l'entité sauvegardée
     * @throws IllegalArgumentException si l'entité ou son identifiant est null
     */
    T save(T entity);

    /**
     * Recherche une entité par son identifiant unique.
     *
     * @param id l'identifiant recherché
     * @return un {@link Optional} contenant l'entité si trouvée, ou {@link Optional#empty()} sinon
     */
    Optional<T> findById(Long id);

    /**
     * Récupère la liste de toutes les entités persistées.
     *
     * @return une liste contenant toutes les entités
     */
    List<T> findAll();

    /**
     * Supprime une entité par son identifiant unique.
     *
     * @param id l'identifiant de l'entité à supprimer
     * @return true si l'entité a été trouvée et supprimée, false sinon
     */
    boolean deleteById(Long id);

    /**
     * Vérifie si une entité existe avec l'identifiant donné.
     *
     * @param id l'identifiant à tester
     * @return true si l'entité existe, false sinon
     */
    boolean existsById(Long id);

    /**
     * Retourne le nombre total d'entités présentes dans le repository.
     *
     * @return le nombre total d'entités
     */
    long count();
}
