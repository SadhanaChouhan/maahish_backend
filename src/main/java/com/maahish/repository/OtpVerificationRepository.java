package com.maahish.repository;

import com.maahish.entity.OtpVerification;
import com.maahish.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
            String email, OtpPurpose purpose);
}
