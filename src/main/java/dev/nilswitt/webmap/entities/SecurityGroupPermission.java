package dev.nilswitt.webmap.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
public class SecurityGroupPermission extends AbstractPermission {

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    @Getter
    @Setter
    private SecurityGroup securityGroup;


    public SecurityGroupPermission() {

    }
}
