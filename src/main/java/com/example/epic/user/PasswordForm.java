package com.example.epic.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordForm {
    @NotBlank(message = "Current password is required.")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    private String newPassword;

    @NotBlank(message = "Please confirm the new password.")
    private String confirmNewPassword;
}
