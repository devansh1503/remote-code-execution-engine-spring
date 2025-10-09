package com.devansh.rceengine.controller;

import com.devansh.rceengine.dto.*;
import com.devansh.rceengine.model.Problem;
import com.devansh.rceengine.service.ChatService;
import com.devansh.rceengine.service.DockerService;
import com.devansh.rceengine.service.JobQueueService;
import com.devansh.rceengine.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CodeExecutionController {

    private final JobQueueService jobQueueService;
    private final ProblemService problemService;
    private final ChatService chatService;

    @PostMapping("/problems")
    public List<Problem> getProblems(@RequestBody ProblemFilter filter){
        return problemService.getProblems(filter.getDifficulty(), filter.getTitle());
    }

    @GetMapping("/problem/{id}")
    public Problem getProblem(@PathVariable Long id){
        return problemService.getProblem(id);
    }

    @PostMapping("/create/problem")
    public Problem createProblem(@RequestBody ProblemRequest request){
        return problemService.createProblem(request);
    }

    @PostMapping("/complexity")
    public ResponseEntity<String> complexityCalculation(@RequestBody ComplexityRequest complexityRequest){
        String response = chatService.complexityAnalysis(
                complexityRequest.getUserCode(),
                complexityRequest.getUserInput()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/execute")
    public CodeJob executeCode(@RequestBody CodeExecutionRequest request){
        CodeJob job= jobQueueService.submitJob(request);
        return job;
    }

    @GetMapping("/result/{jobId}")
    public CodeJob getJobResult(@PathVariable String jobId){
        return jobQueueService.getJob(jobId);
    }
}
