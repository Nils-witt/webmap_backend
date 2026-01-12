package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.MapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MapItemRepository extends JpaRepository<MapItem, UUID> {
    Optional<MapItem> findByName(String name);
}

