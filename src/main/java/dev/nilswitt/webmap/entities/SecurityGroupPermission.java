package dev.nilswitt.webmap.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SecurityGroupPermission extends AbstractPermission {

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    @Getter
    @Setter
    private SecurityGroup securityGroup;

}
