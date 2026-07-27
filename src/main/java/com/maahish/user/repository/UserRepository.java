package com.maahish.user.repository;

import com.maahish.user.entity.User;
import com.maahish.common.enums.UserRole;
import com.maahish.common.enums.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.Collection;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    long countByStatus(UserStatus status);

    java.util.List<User> findByRole(UserRole role);

    org.springframework.data.domain.Page<User> findByStatus(UserStatus status, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByRoleIn(Collection<UserRole> roles, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByStatusAndRoleIn(UserStatus status, Collection<UserRole> roles, org.springframework.data.domain.Pageable pageable);
}
