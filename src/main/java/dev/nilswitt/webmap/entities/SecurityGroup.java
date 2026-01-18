package dev.nilswitt.webmap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class SecurityGroup extends AbstractEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToMany(mappedBy = "securityGroups")
    @JsonIgnore
    private Set<User> users;

    @ManyToMany(mappedBy = "securityGroups")
    @JsonIgnore
    private Set<MapOverlay> overlays;

    @Column
    private Set<String> roles;


    public SecurityGroup() {
    }

    public SecurityGroup(String name) {
        this.name = name;
    }

    public SecurityGroup(String name, Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }


    public enum UserRoleScopeEnum {
        VIEW,
        EDIT,
        CREATE,
        DELETE,
        ADMIN
    }

    public enum UserRoleTypeEnum {
        MAPOVERLAY,
        MAPBASELAYER,
        USER,
        SECURITYGROUP,
        UNIT,
        MAPITEM,
        GLOBAL
    }

    public static List<String> availableRoles() {
        ArrayList<String> roles = new ArrayList<>();
        for (SecurityGroup.UserRoleTypeEnum type : SecurityGroup.UserRoleTypeEnum.values()) {
            for (SecurityGroup.UserRoleScopeEnum scope : SecurityGroup.UserRoleScopeEnum.values()) {
                String roleName = type.name() + "_" + scope.name();
                roles.add(roleName);
            }
        }
        return roles;
    }

    public List<SimpleGrantedAuthority> getGrantedAuthorities() {
        return this.roles.stream()
                .map(a -> new SimpleGrantedAuthority("ROLE_" + a))
                .toList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<MapOverlay> getOverlays() {
        return overlays;
    }

    public void setOverlays(Set<MapOverlay> overlays) {
        this.overlays = overlays;
    }
}