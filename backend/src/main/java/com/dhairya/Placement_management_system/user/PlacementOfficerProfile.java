package com.dhairya.Placement_management_system.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "placement_officer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class PlacementOfficerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "college_name", nullable = false)
    private String collegeName;

    private String department;
}
