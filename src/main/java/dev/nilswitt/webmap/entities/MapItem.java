package dev.nilswitt.webmap.entities;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.reflect.Type;

@Entity
public class MapItem extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String name;


    @Embedded
    private EmbeddedPosition position;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmbeddedPosition getPosition() {
        if (position == null) {
            position = new EmbeddedPosition();
        }
        return position;
    }

    public void setPosition(EmbeddedPosition position) {
        this.position = position;
    }


}