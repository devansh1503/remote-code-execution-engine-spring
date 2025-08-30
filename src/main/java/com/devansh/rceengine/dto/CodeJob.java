package com.devansh.rceengine.dto;

import lombok.Data;

@Data
public class CodeJob {
    public enum Status{
        SUBMITTED,
        RUNNING,
        COMPLETED,
        FAILED
    }
    private String id;
    private Status status;
    private CodeExecutionRequest request;
    private CodeExecutionResult result;
}
