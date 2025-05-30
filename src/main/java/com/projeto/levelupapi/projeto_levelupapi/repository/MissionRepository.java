package com.projeto.levelupapi.projeto_levelupapi.repository;

import com.projeto.levelupapi.projeto_levelupapi.model.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    
    List<Mission> findByIsActiveTrueAndRequiredLevelLessThanEqual(Integer level);
    
    @Query("SELECT m FROM Mission m WHERE m.isActive = true AND m.requiredLevel <= :userLevel")
    List<Mission> findAvailableMissionsForLevel(@Param("userLevel") Integer userLevel);
}
