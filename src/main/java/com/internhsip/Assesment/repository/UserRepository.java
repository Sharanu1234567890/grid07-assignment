package com.internhsip.Assesment.repository;


 import com.internhsip.Assesment.entity.User;
 import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
