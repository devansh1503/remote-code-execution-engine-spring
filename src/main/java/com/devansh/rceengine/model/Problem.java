package com.devansh.rceengine.model;

import com.devansh.rceengine.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problems")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String statement;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String solution;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String driver_code_java;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
}
