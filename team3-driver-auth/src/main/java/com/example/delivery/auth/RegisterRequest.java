package com.example.delivery.auth;
import com.example.delivery.user.Role;
public record RegisterRequest(String email,String password , Role role) {
}
