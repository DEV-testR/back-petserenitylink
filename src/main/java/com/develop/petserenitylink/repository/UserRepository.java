package com.develop.petserenitylink.repository;

import com.develop.petserenitylink.entity.ASUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ASUser, Long> {

    Optional<ASUser> findByEmail(String email);
}
