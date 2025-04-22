package com.example.epic.user;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PwdUpdateDto {
    private String oldPassword;
    private String newPassword;
}
