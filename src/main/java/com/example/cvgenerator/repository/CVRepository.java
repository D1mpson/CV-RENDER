package com.example.cvgenerator.repository;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVRepository extends JpaRepository<CV, Long> {
    List<CV> findByUserOrderByCreatedAtDesc(User user);
}