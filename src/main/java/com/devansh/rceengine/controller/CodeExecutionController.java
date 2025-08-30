package com.devansh.rceengine.controller;

import com.devansh.rceengine.dto.CodeExecutionRequest;
import com.devansh.rceengine.dto.CodeExecutionResult;
import com.devansh.rceengine.service.DockerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/execute")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class CodeExecutionController {
    private final DockerService dockerService;

    @PostMapping
    public CodeExecutionResult executeCode(@RequestBody CodeExecutionRequest request){
        long startTime = System.currentTimeMillis();

        CodeExecutionResult result = new CodeExecutionResult();
        try{
            String output = dockerService.runCode(request.getCode(), request.getStdin(), request.getLanguage());

            result.setOutput(output);
        } catch (Exception e) {
            result.setError("Internal server error: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        result.setExecutionTime(endTime - startTime);
        return result;
    }
}
