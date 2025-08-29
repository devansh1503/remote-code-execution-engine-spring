package com.devansh.rceengine.dto;

import lombok.Data;

@Data
public class CodeExecutionResult {
    private String output;
    private Long executionTime;
    private String error;
}
