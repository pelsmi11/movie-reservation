package com.hectormartinezmoreira.user_service.persistence.repository;

import com.hectormartinezmoreira.user_service.persistence.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
