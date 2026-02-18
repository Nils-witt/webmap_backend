package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.UnitPositionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnitPositionLogRepository extends JpaRepository<UnitPositionLog, UUID> {
    List<UnitPositionLog> findByUnit(Unit unit);


}


