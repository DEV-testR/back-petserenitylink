package com.develop.petserenitylink.controller;

import com.develop.petserenitylink.bean.AuthenticationRequest;
import com.develop.petserenitylink.bean.AuthenticationResponse;
import com.develop.petserenitylink.bean.ChangePasswordRequest;
import com.develop.petserenitylink.bean.UserInfo;
import com.develop.petserenitylink.entity.ASUser;
import com.develop.petserenitylink.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PatchMapping
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal connectedUser
    ) {
        service.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/userInfo")
    public ResponseEntity<UserInfo> getUserInfo(HttpServletRequest request) {
        return ResponseEntity.ok(service.getUserInfo(request));
    }
}
