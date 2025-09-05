package com.example.mercari.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true) 
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
 // 出品先モール名（例：メルカリ、ラクマ、ヤフオク等）
    @ManyToOne
    private Mall mall;

    // 商品名は必須項目
    @NotBlank(message = "商品名は必須です")
    private String title;

    // 商品の価格（販売価格）
    @NotNull(message = "価格は必須です")
    private Integer price = 0;

    // 仕入れ価格
    private Integer cost = 0;

    // 商品管理番号（SKU）
    private String sku;

    // 在庫数
    private Integer stock = 0;

    // JANコード
    private String janCode;

    // 登録日時
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}