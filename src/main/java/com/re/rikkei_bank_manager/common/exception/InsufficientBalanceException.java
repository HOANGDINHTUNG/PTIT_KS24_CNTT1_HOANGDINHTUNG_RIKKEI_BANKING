package com.re.rikkei_bank_manager.common.exception;
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) { super(message); }
}
