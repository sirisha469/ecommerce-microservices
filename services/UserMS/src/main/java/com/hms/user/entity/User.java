package com.hms.user.entity;

import com.hms.user.dto.Roles;
import com.hms.user.dto.UserDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private Roles role;

    public UserDTO toUserDTO(){
        return new UserDTO(this.id, this.name, this.email, this.password, this.role);
    }
}
