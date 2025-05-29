package com.projeto.levelupapi.projeto_levelupapi.repository;

import com.projeto.levelupapi.projeto_levelupapi.model.CompletedMission;
import com.projeto.levelupapi.projeto_levelupapi.model.Mission;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompletedMissionRepository extends JpaRepository<CompletedMission, Long> {
    
    List<CompletedMission> findByUserOrderByCompletedAtDesc(User user);
    
    Optional<CompletedMission> findByUserAndMission(User user, Mission mission);
    
    boolean existsByUserAndMission(User user, Mission mission);
}
