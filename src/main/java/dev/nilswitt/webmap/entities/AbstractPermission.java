package dev.nilswitt.webmap.entities;


import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base for permission assignment, established for which entities permissions can be assigned
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractPermission extends AbstractEntity {

    @Enumerated

    private SecurityGroup.UserRoleScopeEnum scope = SecurityGroup.UserRoleScopeEnum.VIEW;

    @ManyToOne
    @JoinColumn(name = "map_overlay_id")
    private MapOverlay mapOverlay;

    @ManyToOne
    @JoinColumn(name = "map_item_id")
    private MapItem mapItem;

    @ManyToOne
    @JoinColumn(name = "map_group_id")
    private MapGroup mapGroup;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "map_baseLayer_id")
    private MapBaseLayer baseLayer;

    @ManyToOne
    @JoinColumn(name = "entity_user_id")
    private User entityUser;

    @ManyToOne
    @JoinColumn(name = "photo_id")
    private Photo photo;

    @ManyToOne
    @JoinColumn(name = "mission_group_id")
    private MissionGroup missionGroup;

    public AbstractPermission() {

    }
}
