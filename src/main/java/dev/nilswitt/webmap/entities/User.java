package dev.nilswitt.webmap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "username", name = "idx_users_username"),
        @Index(columnList = "email", name = "idx_users_email")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
})
@Getter
public class User extends AbstractEntity implements UserDetails {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String firstName;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = false, length = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 100)
    @JsonIgnore
    private String password = "NaN";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_security_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    @JsonIgnore
    private Set<SecurityGroup> securityGroups = Set.of();

    @Column
    private boolean isEnabled = true;

    @Column
    private boolean isLocked = false;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private Set<UserPermission> userPermissions = new LinkedHashSet<>();

    public User() {
    }

    /**
     * Constructor for creating a user
     *
     * @param username
     * @param email
     * @param firstName
     * @param lastName
     */
    public User(String username, String email, String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
    }


    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.securityGroups.stream().flatMap(securityGroup -> securityGroup.getGrantedAuthorities().stream()).toList();
    }


    public void addSecurityGroup(SecurityGroup securityGroup) {
        if (this.securityGroups == null) {
            this.securityGroups = new HashSet<>();
        }
        this.securityGroups.add(securityGroup);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return this.getId() != null && Objects.equals(this.getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return this.getId() != null ? Objects.hash(this.getId()) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + this.getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + this.getCreatedAt() +
                ", updatedAt=" + this.getUpdatedAt() +
                '}';
    }
}
