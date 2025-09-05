package com.example.mercari.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mercari.entity.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    // 検索用カスタムメソッド例
    List<LedgerEntry> findByDateBetween(LocalDate from, LocalDate to);
    List<LedgerEntry> findByItemNameContaining(String itemName);
    List<LedgerEntry> findBySkuContaining(String sku);
    List<LedgerEntry> findByLedgerPartnerContaining(String ledgerPartner);
    List<LedgerEntry> findByMallContaining(String mall);
    List<LedgerEntry> findByAuctionLotId(Long auctionLotId);
    // 必要なら複合条件の@Queryメソッドも追加可能
}