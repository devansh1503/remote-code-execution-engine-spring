package com.devansh.rceengine.service;

import com.devansh.rceengine.dto.ProblemRequest;
import com.devansh.rceengine.enums.Difficulty;
import com.devansh.rceengine.model.Problem;
import com.devansh.rceengine.repo.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;

    public List<Problem> getProblems(Difficulty difficulty, String title){
        if(difficulty!=null && title!=null){
            return problemRepository.findByDifficultyAndTitle(difficulty, title);
        }
        else if(difficulty!=null){
            return problemRepository.findByDifficulty(difficulty);
        }
        else if(title!=null){
            return problemRepository.findByTitle(title);
        }
        else return problemRepository.findAll();
    }

    public Problem getProblem(Long id){
        Optional<Problem> problemOpt = problemRepository.findById(id);
        if(problemOpt.isPresent()){
            return problemOpt.get();
        }
        throw new RuntimeException("Problem with this ID Not Found "+id);
    }

    public Problem createProblem(ProblemRequest request){
        Problem newProblem = new Problem();
        newProblem.setTitle(request.getTitle());
        newProblem.setStatement(request.getStatement());
        newProblem.setSolution(request.getSolution());
        newProblem.setDriver_code_java(request.getDrive_code_java());
        newProblem.setDifficulty(request.getDifficulty());

        return problemRepository.save(newProblem);
    }
}
