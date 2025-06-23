package com.websistem.websistem.repository;

import com.websistem.websistem.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByJenisTransaksi(String jenisTransaksi);
    List<InventoryTransaction> findByBorrower_Id(Long borrowerId);
    // Tambahkan query lain sesuai kebutuhan
}