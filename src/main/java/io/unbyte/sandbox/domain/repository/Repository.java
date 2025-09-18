package io.unbyte.sandbox.domain.repository;

import io.unbyte.sandbox.domain.model.BaseEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic repository interface for domain entities
 * @param <T> The entity type
 */
public interface Repository<T extends BaseEntity> {
    
    /**
     * Save an entity
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);
    
    /**
     * Find an entity by ID
     * @param id the entity ID
     * @return Optional containing the entity if found
     */
    Optional<T> findById(UUID id);
    
    /**
     * Find all entities
     * @return list of all entities
     */
    List<T> findAll();
    
    /**
     * Delete an entity by ID
     * @param id the entity ID
     */
    void deleteById(UUID id);
    
    /**
     * Check if an entity exists by ID
     * @param id the entity ID
     * @return true if exists, false otherwise
     */
    boolean existsById(UUID id);
}
