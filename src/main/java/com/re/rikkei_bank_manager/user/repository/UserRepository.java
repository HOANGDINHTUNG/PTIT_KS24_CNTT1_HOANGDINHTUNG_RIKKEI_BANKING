package com.re.rikkei_bank_manager.user.repository;

import com.re.rikkei_bank_manager.user.dto.response.UserResponseDto;
import com.re.rikkei_bank_manager.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.re.rikkei_bank_manager.common.enums.RoleName;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("""
            select new com.re.rikkei_bank_manager.user.dto.response.UserResponseDto(
                u.id, u.username, u.phoneNumber, u.email, u.active, u.kyc, u.createdAt, r.name
            )
            from User u join u.role r
            """)
    Page<UserResponseDto> findAllProjected(Pageable pageable);

    @Query("""
            select new com.re.rikkei_bank_manager.user.dto.response.UserResponseDto(
                u.id, u.username, u.phoneNumber, u.email, u.active, u.kyc, u.createdAt, r.name
            )
            from User u join u.role r
            where r.name = :roleName
            """)
    Page<UserResponseDto> findByRoleNameProjected(@Param("roleName") RoleName roleName, Pageable pageable);
}
