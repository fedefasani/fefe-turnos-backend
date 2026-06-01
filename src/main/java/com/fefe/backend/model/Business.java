package com.fefe.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String urlName; // salon-fg

    private String phone;
}