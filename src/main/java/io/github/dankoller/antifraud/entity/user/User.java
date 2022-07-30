package io.github.dankoller.antifraud.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@Entity
@Table(name = "User")
@NoArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    @NotEmpty
    private String name;

    @Column
    @NotEmpty
    private String username;

    @Column
    @NotEmpty
    private String password;

    @Column
    @NotEmpty
    @JsonIgnore
    private String role;

    @Column
    @JsonIgnore
    private boolean isAccountNonLocked;

    // Default constructor for JPA
    public User(String name, String username, String password, String role, boolean isAccountNonLocked) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isAccountNonLocked = isAccountNonLocked;
    }

    /**
     * Returns the plain user role because web security requires a prefix while other methods in this app don't.
     *
     * @return The user's role without the "ROLE_" prefix.
     */
    public String getRoleWithoutPrefix() {
        return role.substring(5);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && isAccountNonLocked == user.isAccountNonLocked
                && Objects.equals(name, user.name)
                && Objects.equals(username, user.username)
                && Objects.equals(password, user.password)
                && Objects.equals(role, user.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, username, password, role, isAccountNonLocked);
    }
}
