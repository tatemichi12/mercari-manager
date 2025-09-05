package com.example.mercari.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "auction_lots")
@Getter
@Setter
public class AuctionLot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date; // 落札日
    private String auctionPartner; // 出品者
    private String address; // 住所
    private String partnerName; // 氏名
    private String mall; // モール
    private int totalPrice; // 合計金額

    @OneToMany(mappedBy = "auctionLot", cascade = CascadeType.ALL)
    private List<LedgerEntry> entries; // 商品一覧（台帳1件ごと）

    // Getter/Setter省略(Lombok利用可能)
}