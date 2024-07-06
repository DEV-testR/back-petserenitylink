package com.develop.petserenitylink.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmationPassword;
}
