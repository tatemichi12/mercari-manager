package com.example.mercari.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "mercari_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MercariItem extends Item {

    // 商品ID
    private String productId;
    
    @Lob
    // 商品説明（任意）
    private String description;

    private String image1;
    private String image2;
    private String image3;
    private String image4;
    private String image5;

    private String skuType;
    private String skuCode;
    private String brandId;

    @NotBlank(message = "カテゴリIDは必須です")
    private String categoryId;

    @NotBlank(message = "商品の状態は必須です")
    @Column(name = "item_condition", nullable = false)
    private String condition = "未設定";

    private String snapshotId;

    // 画像更新フラグ
    private String imageUpdateFlag1;
    private String imageUpdateFlag2;
    private String imageUpdateFlag3;
    private String imageUpdateFlag4;
    private String imageUpdateFlag5;
    private String imageUpdateFlag6;
    private String imageUpdateFlag7;
    private String imageUpdateFlag8;
    private String imageUpdateFlag9;
    private String imageUpdateFlag10;
    private String imageUpdateFlag11;
    private String imageUpdateFlag12;
    private String imageUpdateFlag13;
    private String imageUpdateFlag14;
    private String imageUpdateFlag15;
    private String imageUpdateFlag16;
    private String imageUpdateFlag17;
    private String imageUpdateFlag18;
    private String imageUpdateFlag19;
    private String imageUpdateFlag20;

    private String hash;

    @NotNull(message = "配送方法は必須です")
    private Integer shippingMethod;

    @NotBlank(message = "発送元の地域は必須です")
    private String regionCode;

    @NotNull(message = "発送までの日数は必須です")
    private Integer shippingDays;

    @NotNull(message = "商品ステータスは必須です")
    private Integer status;

    @NotNull(message = "配送料の負担は必須です")
    private Integer shippingCharge;

    private String shippingFeeId;

    @Column(name = "image_serial")
    private String imageSerial;
    
    
}