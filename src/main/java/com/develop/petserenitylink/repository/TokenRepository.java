package com.develop.petserenitylink.repository;

import com.develop.petserenitylink.entity.ASUser;
import com.develop.petserenitylink.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = "select t from Token t inner join ASUser u on t.user.id = u.id where u.id = :id and (t.expired = true or t.revoked = true)")
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);
}
