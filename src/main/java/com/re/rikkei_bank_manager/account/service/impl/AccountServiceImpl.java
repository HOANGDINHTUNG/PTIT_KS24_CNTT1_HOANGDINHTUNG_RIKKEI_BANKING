package com.re.rikkei_bank_manager.account.service.impl;

import com.re.rikkei_bank_manager.account.dto.request.AccountCreateRequest;
import com.re.rikkei_bank_manager.account.dto.request.AccountStatusRequest;
import com.re.rikkei_bank_manager.account.dto.request.PinChangeRequest;
import com.re.rikkei_bank_manager.account.dto.response.AccountResponseDto;
import com.re.rikkei_bank_manager.account.dto.response.BalanceResponse;
import com.re.rikkei_bank_manager.account.entity.Account;
import com.re.rikkei_bank_manager.account.repository.AccountRepository;
import com.re.rikkei_bank_manager.common.exception.*;
import com.re.rikkei_bank_manager.common.util.*;
import com.re.rikkei_bank_manager.user.entity.User;
import com.re.rikkei_bank_manager.user.repository.UserRepository;
import com.re.rikkei_bank_manager.account.mapper.AccountMapper;
import com.re.rikkei_bank_manager.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    @Override
    public List<AccountResponseDto> getCurrentCustomerAccounts() {
        log.info("Bắt đầu xử lý lấy danh sách tài khoản của khách hàng hiện tại.");
        try {
            List<AccountResponseDto> res = accountRepository.findByUserId(getCurrentUser().getId()).stream().map(accountMapper::toResponse).toList();
            log.info("Lấy danh sách tài khoản thành công.");
            return res;
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi lấy danh sách tài khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public BalanceResponse getBalance(Long accountId) {
        log.info("Bắt đầu xử lý lấy số dư cho tài khoản ID: {}", accountId);
        try {
            Account account = findAccount(accountId);
            checkOwnership(account);
            BalanceResponse res = BalanceResponse.builder().accountId(account.getId()).accountNumber(account.getAccountNumber())
                    .balance(account.getBalance()).currency(account.getCurrency()).build();
            log.info("Lấy số dư thành công cho tài khoản ID: {}", accountId);
            return res;
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi tra cứu số dư tài khoản ID {}: {}", accountId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<AccountResponseDto> getAccounts(Pageable pageable) {
        log.info("Bắt đầu xử lý lấy danh sách toàn bộ tài khoản (yêu cầu từ nhân viên).");
        try {
            Page<AccountResponseDto> res = accountRepository.findAll(pageable).map(accountMapper::toResponse);
            log.info("Lấy danh sách tài khoản tổng thành công.");
            return res;
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi lấy danh sách toàn bộ tài khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public AccountResponseDto createAccount(AccountCreateRequest req) {
        log.info("Bắt đầu xử lý mở tài khoản mới cho user ID: {}", req.getUserId());
        try {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + req.getUserId()));
            if (!user.isKyc()) throw new BadRequestException("Cannot create account for user who has not completed KYC");
            String accountNumber = generateUniqueAccountNumber();
            BigDecimal initial = req.getInitialBalance() == null ? BigDecimal.ZERO : req.getInitialBalance();
            Account account = Account.builder()
                    .accountNumber(accountNumber).balance(initial).currency(req.getCurrency())
                    .transactionPin(passwordEncoder.encode(req.getTransactionPin())).active(true).user(user).build();
            AccountResponseDto res = accountMapper.toResponse(accountRepository.save(account));
            log.info("Mở tài khoản thành công cho user ID: {}", req.getUserId());
            return res;
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi khởi tạo tài khoản mới: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public AccountResponseDto changeStatus(Long accountId, AccountStatusRequest req) {
        log.info("Bắt đầu xử lý thay đổi trạng thái hoạt động (kích hoạt: {}) cho tài khoản ID: {}", req.getActive(), accountId);
        try {
            Account account = findAccount(accountId);
            account.setActive(req.getActive());
            log.info("Thay đổi trạng thái tài khoản thành công.");
            return accountMapper.toResponse(account);
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái tài khoản ID {}: {}", accountId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public AccountResponseDto changePin(Long accountId, PinChangeRequest req) {
        log.info("Bắt đầu xử lý đổi mã PIN bảo mật cho tài khoản ID: {}", accountId);
        try {
            Account account = findAccount(accountId);
            checkOwnership(account);
            if (!passwordEncoder.matches(req.getOldPin(), account.getTransactionPin())) throw new ForbiddenException("Old PIN is incorrect");
            account.setTransactionPin(passwordEncoder.encode(req.getNewPin()));
            log.info("Đổi mã PIN thành công cho tài khoản ID: {}", accountId);
            return accountMapper.toResponse(account);
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi thay đổi PIN tài khoản ID {}: {}", accountId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Account findAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    @Override
    public AccountResponseDto getAccount(Long id) {
        return accountMapper.toResponse(findAccount(id));
    }

    @Override
    public void checkOwnership(Account account) {
        if (!account.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ForbiddenException("You can only access your own account");
        }
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new ForbiddenException("Cannot identify current user");
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private String generateUniqueAccountNumber() {
        String number;
        do { number = AccountNumberGenerator.generate(); } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

}
