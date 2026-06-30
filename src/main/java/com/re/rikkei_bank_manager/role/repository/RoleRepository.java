package com.re.rikkei_bank_manager.role.repository;

import com.re.rikkei_bank_manager.common.enums.RoleName;
import com.re.rikkei_bank_manager.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
    boolean existsByName(RoleName name);
}
