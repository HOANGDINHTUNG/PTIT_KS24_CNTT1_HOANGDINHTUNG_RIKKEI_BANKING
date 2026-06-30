package com.re.rikkei_bank_manager.common.util;

import java.security.SecureRandom;

public final class AccountNumberGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private AccountNumberGenerator() {}

    public static String generate() {
        return String.valueOf(100_000_000L + RANDOM.nextInt(900_000_000));
    }
}
