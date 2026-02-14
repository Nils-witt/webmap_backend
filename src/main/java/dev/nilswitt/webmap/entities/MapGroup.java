package dev.nilswitt.webmap.entities;

import dev.nilswitt.webmap.api.dtos.MapGroupDto;
import dev.nilswitt.webmap.entities.eventListeners.EntityEventListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@EntityListeners(EntityEventListener.class)
@Getter
@Setter
public class MapGroup extends AbstractEntity {
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "mapGroup", orphanRemoval = true)
    private Set<MapItem> mapItems = new LinkedHashSet<>();

    @OneToMany(mappedBy = "mapGroup", orphanRemoval = true)
    private Set<MapOverlay> mapOverlays = new LinkedHashSet<>();


    public MapGroupDto toDto() {
        MapGroupDto dto = new MapGroupDto();
        dto.setId(getId());
        dto.setName(getName());
        return dto;
    }
    public static MapGroup of(MapGroupDto dto) {
        MapGroup mapGroup = new MapGroup();
        mapGroup.setName(dto.getName());
        return mapGroup;
    }
}