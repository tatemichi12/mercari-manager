package com.example.mercari.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 入札会商品エンティティ
 * Excelテンプレートのカラム順に対応
 */
@Entity
@Table(name = "auction_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 入札会ID
    @Column(name = "auction_id")
    private String auctionId;

    // 2. グループNo.
    @Column(name = "group_no")
    private String groupNo;

    // 3. 商品No.
    @Column(name = "product_no")
    private String productNo;

    // 4. 入札額
    @Column(name = "bid_amount")
    private Integer bidAmount;

    // 5. グループ名
    @Column(name = "group_name")
    private String groupName;

    // 6. グループ最低入札価格
    @Column(name = "group_min_bid_price")
    private Integer groupMinBidPrice;

    // 7. 商品名
    @Column(name = "product_name")
    private String productName;

    // 8. 備考
    @Column(name = "remarks")
    private String remarks;

    // 9. 商品番号
    @Column(name = "item_number")
    private String itemNumber;

    // 10. メーカー
    @Column(name = "manufacturer")
    private String manufacturer;

    // 11. 型式
    @Column(name = "model")
    private String model;

    // 12. CPU
    @Column(name = "cpu")
    private String cpu;

    // 13. CPUモデル
    @Column(name = "cpu_model")
    private String cpuModel;

    // 14. クロック
    @Column(name = "clock")
    private String clock;

    // 15. メモリ
    @Column(name = "memory")
    private String memory;

    // 16. HDD
    @Column(name = "hdd")
    private String hdd;

    // 17. ドライブ
    @Column(name = "drive")
    private String drive;

    // 18. 無線LAN有無
    @Column(name = "wireless_lan")
    private String wirelessLan;

    // 19. モニタ
    @Column(name = "monitor")
    private String monitor;

    // 20. COA
    @Column(name = "coa")
    private String coa;

    // 21. バッテリ状態
    @Column(name = "battery_condition")
    private String batteryCondition;

    // 22. タイプ
    @Column(name = "type")
    private String type;

    // 23. 不良・欠品内容
    @Column(name = "defect_content")
    private String defectContent;

    // 24. 付属品
    @Column(name = "accessories")
    private String accessories;

    // システム管理項目
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}