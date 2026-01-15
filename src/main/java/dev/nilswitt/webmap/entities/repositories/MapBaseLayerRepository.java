package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.MapBaseLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MapBaseLayerRepository extends JpaRepository<MapBaseLayer, UUID> {
    Optional<MapBaseLayer> findByName(String name);
}
