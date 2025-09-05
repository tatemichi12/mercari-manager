package com.example.mercari.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "rakuten_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RakutenItem extends Item {

    // 楽天-specific fields

    /** 新規(n), 更新(u), 削除(d) */
    @NotBlank
    private String flag;

    /** モバイル版商品名 */
    private String titleMobile;

    /** 楽天商品番号（SKUとは別） */
    @NotBlank
    private String itemNumber;

    /** 楽天商品ID (新規時は空、更新時は変更不可) */
    private String itemId;

    /** 商品ページID（必須） */
    @NotBlank
    private String pageId;

    /** 表示価格 */
    @NotNull
    private Integer displayPrice;

    /** 消費税フラグ: 1=外税, 0=内税 */
    @NotNull
    private Integer taxFlag;

    /** 送料フラグ: 1=送料別, 0=送料込 */
    @NotNull
    private Integer shippingFlag;

    /** 個別送料（半角数値） */
    private Integer individualShipping;

    /** 買い物かごボタン: 1=表示, 0=非表示 */
    @NotNull
    private Integer orderButton;

    /** 資料請求ボタン: 1=表示, 0=非表示 */
    @NotNull
    private Integer documentRequestButton;

    /** 問い合わせボタン: 1=表示, 0=非表示 */
    @NotNull
    private Integer inquiryButton;

    /** おすすめボタン: 1=表示, 0=非表示 */
    @NotNull
    private Integer recommendButton;

    /** のし対応フラグ: 1=対応, 0=非対応 */
    @NotNull
    private Integer noshiFlag;

    /** 在庫数（楽天用ルール: -1は在庫管理しない） */
    @NotNull
    private Integer rakutenStock;

    /** 項目選択肢（例: s_サイズ_S_M_L） */
    private String options;

    /** 期間限定販売（開始） */
    private String limitedSaleStart;

    /** 期間限定販売（終了） */
    private String limitedSaleEnd;

    /** 楽天商品説明（7000文字まで） */
    private String rakutenDescription;

    /** 楽天商品画像ファイル名 */
    private String rakutenImageName;

    /** 楽天ディレクトリID（必須・半角6桁） */
    @NotBlank
    private String directoryId;

    /** モバイル掲載フラグ: 1=掲載, 0=非掲載 */
    @NotNull
    private Integer mobileFlag;
}