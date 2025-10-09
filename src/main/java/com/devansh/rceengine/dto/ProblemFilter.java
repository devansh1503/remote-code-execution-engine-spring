package com.devansh.rceengine.dto;

import com.devansh.rceengine.enums.Difficulty;
import lombok.Data;

@Data
public class ProblemFilter {
    Difficulty difficulty;
    String title;
}
