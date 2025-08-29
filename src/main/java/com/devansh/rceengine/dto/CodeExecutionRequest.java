package com.devansh.rceengine.dto;

import lombok.Data;

@Data
public class CodeExecutionRequest {
    private String code;
    private String language;
    private String stdin;
}
