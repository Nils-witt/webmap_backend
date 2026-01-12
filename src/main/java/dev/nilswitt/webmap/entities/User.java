package dev.nilswitt.webmap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "username", name = "idx_users_username"),
        @Index(columnList = "email", name = "idx_users_email")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
})
public class User extends AbstractEntity {

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
    private String password = "NaN";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnore
    private Set<UserRole> roles;

    public User() {
    }

    public User(String username, String email, String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
    }




    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
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


    static class UserJsonAdaptor implements JsonDeserializer<User>, JsonSerializer<User> {

        @Override
        public User deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            User user = new User();
            if (jsonObject.has("id")) {
                user.id = UUID.fromString(jsonObject.get("id").getAsString());
            }
            if (jsonObject.has("username")) {
                user.username = jsonObject.get("username").getAsString();
            }
            if (jsonObject.has("email")) {
                user.email = jsonObject.get("email").getAsString();
            }
            if (jsonObject.has("firstName")) {
                user.firstName = jsonObject.get("firstName").getAsString();
            }
            if (jsonObject.has("lastName")) {
                user.lastName = jsonObject.get("lastName").getAsString();
            }
            return user;
        }

        @Override
        public JsonElement serialize(User user, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (user.getId() != null) {
                jsonObject.addProperty("id", user.getId().toString());
            }
            jsonObject.addProperty("username", user.getUsername());
            jsonObject.addProperty("email", user.getEmail());
            jsonObject.addProperty("firstName", user.getFirstName());
            jsonObject.addProperty("lastName", user.getLastName());
            return jsonObject;
        }
    }
}
