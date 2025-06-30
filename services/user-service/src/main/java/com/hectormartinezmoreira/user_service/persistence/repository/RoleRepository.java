package com.hectormartinezmoreira.user_service.persistence.repository;

import com.hectormartinezmoreira.user_service.persistence.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    /**
     * Finds a role by its name.
     *
     * @param name the role name
     * @return an optional containing the role, if found
     */
    Optional<Role> findByName(String name);
}
