package sn.codesamb.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implémentation générique en mémoire de l'interface {@link Repository}.
 * <p>
 * Les entités sont stockées dans une {@link LinkedHashMap} en conservant l'ordre d'insertion.
 *
 * @param <T> le type de l'entité
 */
public class InMemoryRepository<T> implements Repository<T> {

    protected final Map<Long, T> storage = new LinkedHashMap<>();
    private final Function<T, Long> idExtractor;

    /**
     * Initialise le repository avec la fonction d'extraction de l'identifiant.
     *
     * @param idExtractor fonction extrayant l'identifiant {@link Long} d'une entité {@code T}
     */
    public InMemoryRepository(Function<T, Long> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor, "L'extracteur d'identifiant est obligatoire.");
    }

    /**
     * Extrait l'identifiant d'une entité donnée.
     *
     * @param entity l'entité
     * @return l'identifiant {@link Long}
     */
    protected Long getId(T entity) {
        return idExtractor.apply(entity);
    }

    @Override
    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité à sauvegarder ne peut pas être null.");
        }
        Long id = getId(entity);
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant de l'entité ne peut pas être null.");
        }
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.containsKey(id);
    }

    @Override
    public long count() {
        return storage.size();
    }
}
