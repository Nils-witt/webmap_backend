package dev.nilswitt.webmap.entities;


import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
public abstract class AbstractPermission extends AbstractEntity {

    @Enumerated
    @Getter
    @Setter
    private SecurityGroup.UserRoleScopeEnum scope = SecurityGroup.UserRoleScopeEnum.VIEW;

    @ManyToOne
    @JoinColumn(name = "map_overlay_id")
    @Getter
    @Setter
    private MapOverlay mapOverlay;

    @ManyToOne
    @JoinColumn(name = "map_item_id")
    @Getter
    @Setter
    private MapItem mapItem;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @Getter
    @Setter
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "map_baseLayer_id")
    @Getter
    @Setter
    private MapBaseLayer baseLayer;

    @ManyToOne
    @JoinColumn(name = "entity_user_id")
    @Getter
    @Setter
    private User entityUser;

    public AbstractPermission() {

    }
}
