package com.pavan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pavan.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
