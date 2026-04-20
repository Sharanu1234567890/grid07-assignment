package com.internhsip.Assesment.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bots")
@Getter @Setter
@NoArgsConstructor
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String personaDescription;
}