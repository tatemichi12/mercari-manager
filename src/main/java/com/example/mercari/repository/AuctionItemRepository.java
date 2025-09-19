package com.example.mercari.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.mercari.entity.AuctionItem;

@Repository
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
    
    // 入札会IDとグループNo.と商品No.で一意検索（重複防止用）
    Optional<AuctionItem> findByAuctionIdAndGroupNoAndProductNo(
        String auctionId, String groupNo, String productNo);
    
    // 入札会IDで検索
    List<AuctionItem> findByAuctionId(String auctionId);
    
    // グループNo.で検索
    List<AuctionItem> findByGroupNo(String groupNo);
    
    // 商品名での部分検索
    List<AuctionItem> findByProductNameContaining(String productName);
    
    // メーカーでの検索
    List<AuctionItem> findByManufacturer(String manufacturer);
    
    // 全項目横断検索
    @Query("""
            SELECT a FROM AuctionItem a
            WHERE (:keyword IS NULL OR :keyword = ''
                OR a.auctionId LIKE CONCAT('%', :keyword, '%')
                OR a.groupNo LIKE CONCAT('%', :keyword, '%')
                OR a.productNo LIKE CONCAT('%', :keyword, '%')
                OR a.groupName LIKE CONCAT('%', :keyword, '%')
                OR a.productName LIKE CONCAT('%', :keyword, '%')
                OR a.manufacturer LIKE CONCAT('%', :keyword, '%')
                OR a.model LIKE CONCAT('%', :keyword, '%')
                OR a.itemNumber LIKE CONCAT('%', :keyword, '%')
                OR a.remarks LIKE CONCAT('%', :keyword, '%')
            )
            ORDER BY a.auctionId, a.groupNo, a.productNo
        """)
    List<AuctionItem> searchAll(@Param("keyword") String keyword);
}