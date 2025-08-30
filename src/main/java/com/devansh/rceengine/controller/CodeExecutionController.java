package com.devansh.rceengine.controller;

import com.devansh.rceengine.dto.CodeExecutionRequest;
import com.devansh.rceengine.dto.CodeExecutionResult;
import com.devansh.rceengine.dto.CodeJob;
import com.devansh.rceengine.service.DockerService;
import com.devansh.rceengine.service.JobQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CodeExecutionController {

    private final JobQueueService jobQueueService;

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
