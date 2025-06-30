package com.hectormartinezmoreira.user_service.persistence.repository;

import com.hectormartinezmoreira.user_service.persistence.model.UserRole;
import com.hectormartinezmoreira.user_service.persistence.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
