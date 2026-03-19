package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.UnitPositionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UnitPositionLogRepository extends JpaRepository<UnitPositionLog, UUID> {
    List<UnitPositionLog> findByUnit(Unit unit);

    List<UnitPositionLog> findByUnitAndPosition_TimestampAfter(Unit unit, Instant timestamp);


}