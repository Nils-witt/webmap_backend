package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.MapOverlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MapOverlayRepository extends JpaRepository<MapOverlay, UUID> {
    Optional<MapOverlay> findByName(String name);
}
