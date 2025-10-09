package com.devansh.rceengine.dto;

import com.devansh.rceengine.enums.Difficulty;
import lombok.Data;

@Data
public class ProblemRequest {
    String title;
    String statement;
    String solution;
    String drive_code_java;
    Difficulty difficulty;
}
