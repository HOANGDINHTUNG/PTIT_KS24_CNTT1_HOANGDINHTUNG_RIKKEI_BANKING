package com.re.rikkei_bank_manager.transaction.service.impl;

import com.re.rikkei_bank_manager.account.entity.Account;
import com.re.rikkei_bank_manager.account.repository.AccountRepository;
import com.re.rikkei_bank_manager.common.enums.*;
import com.re.rikkei_bank_manager.common.exception.*;
import com.re.rikkei_bank_manager.common.util.SecurityUtils;
import com.re.rikkei_bank_manager.transaction.dto.request.TransferRequest;
import com.re.rikkei_bank_manager.transaction.dto.response.StatementResponse;
import com.re.rikkei_bank_manager.transaction.dto.response.TransferResponse;
import com.re.rikkei_bank_manager.transaction.entity.BankTransaction;
import com.re.rikkei_bank_manager.transaction.repository.TransactionRepository;
import com.re.rikkei_bank_manager.user.entity.User;
import com.re.rikkei_bank_manager.user.repository.UserRepository;
import com.re.rikkei_bank_manager.transaction.mapper.TransactionMapper;
import com.re.rikkei_bank_manager.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Service @RequiredArgsConstructor @Transactional(readOnly = true) @Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest req) {
        log.info("Bắt đầu xử lý giao dịch chuyển khoản từ TK {} sang TK {} với số tiền: {}", req.getFromAccountNumber(), req.getToAccountNumber(), req.getAmount());
        try {
            User user = getCurrentUser();
            if (!user.isKyc()) throw new ForbiddenException("User must complete KYC before transferring money");
            if (java.util.Objects.equals(req.getFromAccountNumber(), req.getToAccountNumber())) throw new BadRequestException("Cannot transfer to the same account");

            Set<String> numbers = new HashSet<>(List.of(req.getFromAccountNumber(), req.getToAccountNumber()));
            List<Account> locked = accountRepository.findByAccountNumbersForUpdate(numbers);
            if (locked.size() != 2) throw new ResourceNotFoundException("Source account or target account not found");

            Account from = locked.stream().filter(a -> java.util.Objects.equals(a.getAccountNumber(), req.getFromAccountNumber())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
            Account to = locked.stream().filter(a -> java.util.Objects.equals(a.getAccountNumber(), req.getToAccountNumber())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Target account not found"));

            validateTransfer(user, from, to, req);
            BigDecimal amount = req.getAmount();
            from.setBalance(from.getBalance().subtract(amount));
            to.setBalance(to.getBalance().add(amount));

            BankTransaction tx = transactionRepository.save(BankTransaction.builder()
                    .transactionCode("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase())
                    .fromAccount(from).toAccount(to).amount(amount).description(req.getDescription())
                    .status(TransactionStatus.SUCCESS).createdAt(LocalDateTime.now()).build());

            log.info("Giao dịch chuyển tiền thành công với mã giao dịch: {}", tx.getTransactionCode());
            return transactionMapper.toTransferResponse(tx);
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi trong quá trình chuyển khoản: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<StatementResponse> getStatement(Long accountId, Pageable pageable) {
        log.info("Bắt đầu xử lý lấy sao kê giao dịch cho tài khoản ID: {}", accountId);
        try {
            User user = getCurrentUser();
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
            if (!java.util.Objects.equals(account.getUser().getId(), user.getId())) throw new ForbiddenException("You can only view statement of your own account");
            
            Page<StatementResponse> response = transactionRepository.findStatementByAccountId(accountId, pageable).map(tx -> transactionMapper.toStatementResponse(tx, accountId));
            log.info("Lấy sao kê giao dịch thành công cho tài khoản ID: {}", accountId);
            return response;
        } catch (Exception e) {
            log.error("Đã xảy ra lỗi khi lấy sao kê cho tài khoản ID {}: {}", accountId, e.getMessage(), e);
            throw e;
        }
    }

    private void validateTransfer(User user, Account from, Account to, TransferRequest req) {
        if (!java.util.Objects.equals(from.getUser().getId(), user.getId())) throw new ForbiddenException("Source account does not belong to current user");
        if (!from.isActive()) throw new BadRequestException("Source account is inactive");
        if (!to.isActive()) throw new BadRequestException("Target account is inactive");
        if (!passwordEncoder.matches(req.getTransactionPin(), from.getTransactionPin())) throw new ForbiddenException("Transaction PIN is incorrect");
        if (from.getBalance().compareTo(req.getAmount()) < 0) throw new InsufficientBalanceException("Insufficient balance");
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new ForbiddenException("Cannot identify current user");
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
