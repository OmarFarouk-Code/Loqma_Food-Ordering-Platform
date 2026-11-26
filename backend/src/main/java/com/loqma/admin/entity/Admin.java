package com.loqma.admin.entity;

import com.loqma.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    @Column(name = "admin_code", length = 50, nullable = false, unique = true)
    private String adminCode;
}