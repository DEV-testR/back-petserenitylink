package com.develop.petserenitylink.service;

import com.develop.petserenitylink.bean.AuthenticationRequest;
import com.develop.petserenitylink.bean.AuthenticationResponse;
import com.develop.petserenitylink.bean.RegisterRequest;
import com.develop.petserenitylink.bean.Role;
import com.develop.petserenitylink.bean.TokenType;
import com.develop.petserenitylink.bean.UserInfo;
import com.develop.petserenitylink.entity.ASUser;
import com.develop.petserenitylink.entity.Token;
import com.develop.petserenitylink.repository.TokenRepository;
import com.develop.petserenitylink.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = ASUser.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        UsernamePasswordAuthenticationToken userAuth = new UsernamePasswordAuthenticationToken(email,password);
        authenticationManager.authenticate(userAuth);

        var user = repository.findByEmail(email).orElseThrow();
        var userInfo = UserInfo.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole()).build();

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userInfo)
                .build();
    }

    private void saveUserToken(ASUser user, String accessToken) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        Token tokenUser = validUserTokens.stream().findFirst().orElse(null);
        Token token = (tokenUser == null) ? new Token() : tokenUser;

        token.setUser(user);
        token.setToken(accessToken);
        token.setTokenType(TokenType.BEARER);
        token.setExpired(true);
        token.setRevoked(true);
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(ASUser user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }

        validUserTokens.forEach(token -> {
            token.setToken("");
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        var user = this.repository.findByEmail(userEmail).orElseThrow();
        if (!jwtService.isTokenValid(refreshToken, user)) {
            return;
        }

        var newAccessToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, newRefreshToken);
        var authResponse = AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }
}
