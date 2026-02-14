package dev.nilswitt.webmap.entities;


import dev.nilswitt.webmap.api.dtos.AbstractEntityDto;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
public class UserPermission extends AbstractPermission {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Getter
    @Setter
    private User user;


    public UserPermission() {

    }


    public AbstractEntityDto toDto() {
        AbstractEntityDto dto = new AbstractEntityDto();
        dto.setId(getId());
        return dto;
    }

}
