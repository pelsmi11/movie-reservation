package com.hectormartinezmoreira.user_service.persistence.repository;

import com.hectormartinezmoreira.user_service.persistence.model.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}
