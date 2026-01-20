package dev.nilswitt.webmap.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.nilswitt.webmap.entities.eventListeners.EntityEventListener;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@Entity
@EntityListeners(EntityEventListener.class)
public class MapOverlay extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String baseUrl = "";

    @Column(nullable = false, length = 100)
    private String basePath = "";

    @Column
    private String tilePathPattern = "/{z}/{x}/{y}.png";

    @Column
    private int layerVersion = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "map_overlay_security_group",
            joinColumns = @JoinColumn(name = "overlay_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    @JsonIgnore
    private Set<SecurityGroup> securityGroups;

    @JsonGetter("fullTileUrl")
    public String getFullTileUrl() {
        String baseUrl = getBaseUrl();
        String basePath = getBasePath();
        String tilePathPattern = getTilePathPattern();

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }
        if (!tilePathPattern.startsWith("/")) {
            tilePathPattern = "/" + tilePathPattern;
        }

        StringBuilder fullUrl = new StringBuilder();
        fullUrl.append(baseUrl);
        fullUrl.append(basePath);
        fullUrl.append(this.getId()).append("/");
        fullUrl.append(this.layerVersion);
        fullUrl.append(tilePathPattern);
        return fullUrl.toString();
    }

}