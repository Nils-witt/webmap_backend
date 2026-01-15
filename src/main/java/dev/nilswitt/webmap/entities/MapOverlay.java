package dev.nilswitt.webmap.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import dev.nilswitt.webmap.entities.eventListeners.EntityEventListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public int getLayerVersion() {
        return layerVersion;
    }

    public void setLayerVersion(int layerVersion) {
        this.layerVersion = layerVersion;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getTilePathPattern() {
        return tilePathPattern;
    }

    public void setTilePathPattern(String tilePathPattern) {
        this.tilePathPattern = tilePathPattern;
    }
}