package com.example.mercari.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate date;       // 年月日
    private String type;          // 区別
    private String sku;           // 自社コード
    private String feature;       // 特徴
    private int quantity;         // 数量
    private int price;            // 代価
    private String itemName;      // 品目
    private String ledgerPartner; // 取引きの相手方
    private String address;       // 住所
    private String partnerName;   // 氏名
    private String mall;          // 取引モール
    
    private String auctionPartner;
    
    @ManyToOne
    @JoinColumn(name = "auction_lots_id")
    private AuctionLot auctionLot; // 親オークション
}