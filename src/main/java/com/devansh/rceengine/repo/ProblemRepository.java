package com.devansh.rceengine.repo;

import com.devansh.rceengine.enums.Difficulty;
import com.devansh.rceengine.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(Difficulty difficulty);
    List<Problem> findByTitle(String title);
    List<Problem> findByDifficultyAndTitle(Difficulty difficulty, String title);
}
