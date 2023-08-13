package com.inbank.dengine.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthenticationRequest {
    @NotNull(message = "username is mandatory")
    private String username;
    @NotNull(message = "password is mandatory")
    private String password;
}
