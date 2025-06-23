package com.websistem.websistem.repository;

import com.websistem.websistem.model.InventoryItem;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Page<InventoryItem> findByKodeBarangContainingIgnoreCase(String kodeBarang, Pageable pageable);
    Page<InventoryItem> findAll(Pageable pageable);
    boolean existsByKodeBarang(String kodeBarang);
}