package com.re.rikkei_bank_manager.transaction.repository;

import com.re.rikkei_bank_manager.transaction.entity.BankTransaction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {
    @Query("""
            select t from BankTransaction t
            where t.fromAccount.id = :accountId or t.toAccount.id = :accountId
            order by t.createdAt desc
            """)
    Page<BankTransaction> findStatementByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM BankTransaction t WHERE t.fromAccount.id = :accountId AND t.createdAt BETWEEN :startOfDay AND :endOfDay AND t.status = 'SUCCESS'")
    java.math.BigDecimal calculateDailyTransferSum(@Param("accountId") Long accountId, @Param("startOfDay") java.time.LocalDateTime startOfDay, @Param("endOfDay") java.time.LocalDateTime endOfDay);
}
