package com.maahish.repository;

import com.maahish.entity.RefreshToken;
import com.maahish.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByUserAndRevokedTrue(User user);
}
