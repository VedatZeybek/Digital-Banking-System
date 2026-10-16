package com.example.banking.repository;

import com.example.banking.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {
    @Query("""
        select t from BankTransaction t
        left join fetch t.fromAccount fa
        left join fetch t.toAccount ta
        where fa.id = :accountId or ta.id = :accountId
        order by t.createdAt desc, t.id desc
        """)
    List<BankTransaction> findHistory(@Param("accountId") Long accountId);
}

