package com.example.mercari.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mercari.entity.AuctionLot;

public interface AuctionLotRepository extends JpaRepository<AuctionLot, Long> {

    List<AuctionLot> findByDateGreaterThanEqualAndAuctionPartner(LocalDate date, String auctionPartner);
    List<AuctionLot> findByDateAndAuctionPartner(LocalDate date, String auctionPartner);
    // 必要ならカスタム検索メソッド追加可能
}