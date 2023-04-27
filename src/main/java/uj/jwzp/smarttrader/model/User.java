package uj.jwzp.smarttrader.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "'USER'")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name="user_roles", joinColumns = @JoinColumn(name ="user_id", referencedColumnName = "id"),
            inverseJoinColumns=@JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private List<Role> roles= new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
