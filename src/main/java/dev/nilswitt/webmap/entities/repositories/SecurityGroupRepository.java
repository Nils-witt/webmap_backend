package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.SecurityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityGroupRepository extends JpaRepository<SecurityGroup, UUID> {
    Optional<SecurityGroup> findByName(String name);

}
