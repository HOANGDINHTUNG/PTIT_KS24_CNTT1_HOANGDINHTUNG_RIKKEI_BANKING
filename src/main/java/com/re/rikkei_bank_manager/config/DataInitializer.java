package com.re.rikkei_bank_manager.config;

import com.re.rikkei_bank_manager.account.entity.Account;
import com.re.rikkei_bank_manager.account.repository.AccountRepository;
import com.re.rikkei_bank_manager.common.enums.*;
import com.re.rikkei_bank_manager.kyc.entity.KycProfile;
import com.re.rikkei_bank_manager.kyc.repository.KycProfileRepository;
import com.re.rikkei_bank_manager.role.entity.Role;
import com.re.rikkei_bank_manager.role.repository.RoleRepository;
import com.re.rikkei_bank_manager.user.entity.User;
import com.re.rikkei_bank_manager.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.*;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(RoleRepository roleRepo, UserRepository userRepo, AccountRepository accRepo,
                           KycProfileRepository kycRepo, PasswordEncoder encoder) {
        return args -> {
            Role adminRole = roleRepo.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepo.save(Role.builder().name(RoleName.ADMIN).description("Quản trị hệ thống").build()));
            Role staffRole = roleRepo.findByName(RoleName.STAFF)
                    .orElseGet(() -> roleRepo.save(Role.builder().name(RoleName.STAFF).description("Giao dịch viên").build()));
            Role customerRole = roleRepo.findByName(RoleName.CUSTOMER)
                    .orElseGet(() -> roleRepo.save(Role.builder().name(RoleName.CUSTOMER).description("Khách hàng").build()));

            User admin = seedUser(userRepo, encoder, "admin", "admin123", "admin@rikkeibank.com", "0900000001", true, true, adminRole);
            User staff = seedUser(userRepo, encoder, "staff", "staff123", "staff@rikkeibank.com", "0900000002", true, true, staffRole);
            User c1 = seedUser(userRepo, encoder, "customer1", "customer123", "customer1@rikkeibank.com", "0900000003", true, true, customerRole);
            User c2 = seedUser(userRepo, encoder, "customer2", "customer123", "customer2@rikkeibank.com", "0900000004", true, true, customerRole);

            seedKyc(kycRepo, c1, "001201000001", "Nguyen Van Customer One");
            seedKyc(kycRepo, c2, "001201000002", "Tran Thi Customer Two");
            seedAccount(accRepo, encoder, c1, "100000001", new BigDecimal("10000000.00"));
            seedAccount(accRepo, encoder, c2, "100000002", new BigDecimal("1000000.00"));
        };
    }

    private User seedUser(UserRepository repo, PasswordEncoder encoder, String username, String password,
                          String email, String phone, boolean active, boolean kyc, Role role) {
        return repo.findByUsername(username).orElseGet(() -> repo.save(User.builder()
                .username(username).password(encoder.encode(password)).email(email).phoneNumber(phone)
                .active(active).kyc(kyc).role(role).build()));
    }

    private void seedKyc(KycProfileRepository repo, User user, String idNumber, String fullName) {
        repo.findByUserId(user.getId()).orElseGet(() -> repo.save(KycProfile.builder()
                .user(user).idNumber(idNumber).fullName(fullName).dob(LocalDate.of(2001,1,1))
                .sex("MALE").address("Ha Noi, Viet Nam").idCardFrontUrl("seed-data")
                .status(KycStatus.CONFIRM).verifiedAt(LocalDateTime.now()).build()));
    }

    private void seedAccount(AccountRepository repo, PasswordEncoder encoder, User user, String number, BigDecimal balance) {
        repo.findByAccountNumber(number).orElseGet(() -> repo.save(Account.builder()
                .accountNumber(number).balance(balance).currency(Currency.VND)
                .transactionPin(encoder.encode("123456")).active(true).user(user).build()));
    }
}
