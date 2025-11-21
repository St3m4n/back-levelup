package com.levelup.backend.repository;

import com.levelup.backend.model.LevelUpStats;
import com.levelup.backend.model.Referral;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    boolean existsByStatsAndEmail(LevelUpStats stats, String email);
    List<Referral> findByStatsOrderByCreatedAtDesc(LevelUpStats stats);
    List<Referral> findByStatsInOrderByCreatedAtDesc(Collection<LevelUpStats> stats);
}
