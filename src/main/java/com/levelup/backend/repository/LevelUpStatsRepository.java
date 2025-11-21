package com.levelup.backend.repository;

import com.levelup.backend.model.LevelUpStats;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LevelUpStatsRepository extends JpaRepository<LevelUpStats, Long> {
    Optional<LevelUpStats> findByUsuarioRun(String run);
    Optional<LevelUpStats> findByReferralCode(String referralCode);
    Optional<LevelUpStats> findByRun(String run);
    boolean existsByReferralCode(String referralCode);
    List<LevelUpStats> findByRunIn(Collection<String> runs);
}
