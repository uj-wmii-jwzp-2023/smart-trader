package uj.jwzp.smarttrader.model;

import com.mongodb.client.model.geojson.Position;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document("User")
public class User {
    @Id
    @Null(message = "Id is generated automatically.")
    private String id;
    @Indexed(unique=true)
    @NotNull(message = "Username is required.")
    private String name;
    @NotNull(message = "Password is required.")
    private String password;
    @NotNull(message = "Roles are required.")
    private List<Role> roles;
    @Null(message = "Assets are set by executing orders.")
    private List<Asset> assets;
    @Null(message = "Cash balance can be set with deposit and withdraw options.")
    private BigDecimal cashBalance;

    public User(@NotNull(message = "Username is required.") String name,
                @NotNull(message = "Password is required.") String password,
                @NotNull(message = "Roles are required.") List<Role> roles) {
        this.name = name;
        this.password = password;
        this.roles = roles;
        this.assets = new ArrayList<>();
        this.cashBalance = new BigDecimal(0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }
}