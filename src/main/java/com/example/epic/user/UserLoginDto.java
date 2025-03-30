package com.example.epic.user;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLoginDto {
    private Long id;

    private String username;

    private String password;

    @Email
    private String email;

    private String user_level;

    private LocalDateTime last_tested_at;

    public SiteUser toEntity() {
        return new SiteUser(id, username, password, email, user_level, last_tested_at);
    }
}
