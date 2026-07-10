package com.example.shiftplanner_server.repositories;

import com.example.shiftplanner_server.entities.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Integer> {
//    default List<Policy> getAllPolicies() {
//        return findAll();
//    }
//
//    default Policy savePolicy(Policy policy) {
//        return save(policy);
//    }
}

