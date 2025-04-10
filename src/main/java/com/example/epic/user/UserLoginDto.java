package com.example.epic.user;

import jakarta.validation.constraints.Email;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLoginDto {
    @Email
    private String email;
    private String password;
}
