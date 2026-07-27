package com.maahish.auth.repository;

import com.maahish.auth.enums.OtpPurpose;
import com.maahish.auth.entity.OtpVerification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
            String email, OtpPurpose purpose);

    Optional<OtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    long countByEmailAndPurposeAndCreatedAtAfter(String email, OtpPurpose purpose, LocalDateTime after);
}
