package com.example.epic.stats;

import com.example.epic.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningStatisticsRepository extends JpaRepository<LearningStatistics, Long> {
    Optional<LearningStatistics> findByUser(SiteUser user);
}
