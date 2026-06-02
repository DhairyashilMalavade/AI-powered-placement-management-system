package com.dhairya.Placement_management_system.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(u) FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchByFullNameOrEmail(String search, Pageable pageable);

    @Query(value = "SELECT u FROM User u WHERE (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND u.role = :role",
           countQuery = "SELECT COUNT(u) FROM User u WHERE (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND u.role = :role")
    Page<User> searchByFullNameEmailAndRole(String search, String role, Pageable pageable);

    long countByRole(String role);
}
