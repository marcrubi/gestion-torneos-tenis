package com.ISII.gestion_torneos_tenis.repository;

import com.ISII.gestion_torneos_tenis.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
}