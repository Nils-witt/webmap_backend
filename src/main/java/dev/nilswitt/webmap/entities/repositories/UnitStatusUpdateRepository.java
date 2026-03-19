package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.UnitStatusUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnitStatusUpdateRepository extends JpaRepository<UnitStatusUpdate, UUID> {

    List<UnitStatusUpdate> findByUnit(Unit unit);

    UnitStatusUpdate findFirstByUnitOrderByCreatedAtAsc(Unit unit);

}