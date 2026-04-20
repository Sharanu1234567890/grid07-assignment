package com.internhsip.Assesment.repository;


 import com.internhsip.Assesment.entity.Post;
 import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
