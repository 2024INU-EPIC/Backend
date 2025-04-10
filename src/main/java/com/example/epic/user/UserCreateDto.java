package com.example.epic.user;

import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserCreateDto {
    @Null
    private Long id;

    @Length(min = 3, max = 20)
    @NotEmpty(message = "사용자 이름은 필수항목")
    private String username;

    @NotEmpty(message = "사용자 비밀번호는 필수항목")
    @Size(min = 7, max = 20)
    private String password1;

    @NotEmpty(message = "비밀번호 확인은 필수 항목")
    @Size(min = 7, max = 20)
    private String password2;

    @NotEmpty(message = "이메일은 필수항목")
    @Email
    private String email;

    @Null
    private String user_level;

    @Null
    private LocalDateTime last_tested_at;
}