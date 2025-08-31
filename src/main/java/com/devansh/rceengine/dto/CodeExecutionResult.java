package com.devansh.rceengine.dto;

import lombok.Data;

@Data
public class CodeExecutionResult {
    private String output;
    private String executionTime;
    private String error;
}
