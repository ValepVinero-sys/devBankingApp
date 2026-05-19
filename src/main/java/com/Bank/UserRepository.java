package com.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
    @Repository
    public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String Email);
        boolean existsByEmail(String Email);
        @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.email = :email")
        Optional<User> findByEmailWithAccounts(@Param("email") String Email);
    }

