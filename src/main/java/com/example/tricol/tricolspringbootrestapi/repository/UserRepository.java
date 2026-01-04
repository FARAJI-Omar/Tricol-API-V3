package com.example.tricol.tricolspringbootrestapi.repository;

import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserApp, Long> {
    Optional<UserApp> findByUsername(String username);

    @Query("SELECT u FROM UserApp u LEFT JOIN FETCH u.userPermissions WHERE u.username = :username")
    Optional<UserApp> findByUsernameWithPermissions(@Param("username") String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
