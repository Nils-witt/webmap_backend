package dev.nilswitt.webmap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "units")
public class Unit extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String name;


    @Embedded
    private EmbeddedPosition position;

    @Column(nullable = false)
    private int status = 6;

    @Column(nullable = false)
    private boolean speakRequest = false;


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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSpeakRequest() {
        return speakRequest;
    }

    public void setSpeakRequest(boolean speakRequest) {
        this.speakRequest = speakRequest;
    }

}