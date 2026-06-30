package com.re.rikkei_bank_manager.account.service;

import com.re.rikkei_bank_manager.account.dto.request.AccountCreateRequest;
import com.re.rikkei_bank_manager.account.dto.request.AccountStatusRequest;
import com.re.rikkei_bank_manager.account.dto.request.PinChangeRequest;
import com.re.rikkei_bank_manager.account.dto.response.AccountResponseDto;
import com.re.rikkei_bank_manager.account.dto.response.BalanceResponse;
import com.re.rikkei_bank_manager.account.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {
    List<AccountResponseDto> getCurrentCustomerAccounts();
    BalanceResponse getBalance(Long accountId);
    Page<AccountResponseDto> getAccounts(Pageable pageable);
    AccountResponseDto createAccount(AccountCreateRequest req);
    AccountResponseDto changeStatus(Long accountId, AccountStatusRequest req);
    AccountResponseDto changePin(Long accountId, PinChangeRequest req);
    Account findAccount(Long id);
    AccountResponseDto getAccount(Long id);
    void checkOwnership(Account account);
}
