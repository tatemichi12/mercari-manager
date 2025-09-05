package com.example.mercari.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.mercari.entity.MercariItem;

@Repository
public interface MercariItemRepository extends JpaRepository<MercariItem, Long> {
    Optional<MercariItem> findByProductId(String productId);

    List<MercariItem> findBySku(String sku);

    // 全項目検索（descriptionのみLOWER()を外す！）
    @Query("""
            SELECT i FROM MercariItem i
            LEFT JOIN i.mall m
            WHERE i.status = :status AND (
                :keyword IS NULL OR :keyword = ''
                OR i.title LIKE CONCAT('%', :keyword, '%')
                OR i.sku LIKE CONCAT('%', :keyword, '%')
                OR i.description LIKE CONCAT('%', :keyword, '%')
                OR i.categoryId LIKE CONCAT('%', :keyword, '%')
                OR i.brandId LIKE CONCAT('%', :keyword, '%')
                OR i.janCode LIKE CONCAT('%', :keyword, '%')
                OR CAST(i.price AS string) LIKE CONCAT('%', :keyword, '%')
                OR CAST(i.stock AS string) LIKE CONCAT('%', :keyword, '%')
                OR (m IS NOT NULL AND m.name LIKE CONCAT('%', :keyword, '%'))
                OR (m IS NOT NULL AND m.code LIKE CONCAT('%', :keyword, '%'))
            )
        """)
        List<MercariItem> searchAllByStatus(@Param("status") Integer status, @Param("keyword") String keyword);

        List<MercariItem> findByStatus(Integer status);

        @Query("""
            SELECT i FROM MercariItem i
            LEFT JOIN i.mall m
            WHERE (:keyword IS NULL OR :keyword = ''
                OR i.title LIKE CONCAT('%', :keyword, '%')
                OR i.sku LIKE CONCAT('%', :keyword, '%')
                OR i.description LIKE CONCAT('%', :keyword, '%')
                OR i.categoryId LIKE CONCAT('%', :keyword, '%')
                OR i.brandId LIKE CONCAT('%', :keyword, '%')
                OR i.janCode LIKE CONCAT('%', :keyword, '%')
                OR CAST(i.price AS string) LIKE CONCAT('%', :keyword, '%')
                OR CAST(i.stock AS string) LIKE CONCAT('%', :keyword, '%')
                OR (m IS NOT NULL AND m.name LIKE CONCAT('%', :keyword, '%'))
                OR (m IS NOT NULL AND m.code LIKE CONCAT('%', :keyword, '%'))
            )
        """)
        List<MercariItem> searchAll(@Param("keyword") String keyword);
    
    @Modifying
    @Query("UPDATE MercariItem i SET i.shippingDays = :shippingDays WHERE i.id IN :ids")
    void updateShippingDaysForIds(@Param("shippingDays") Integer shippingDays,
                                  @Param("ids") List<Long> ids);

    @Query("SELECT i.sku, SUM(i.stock) FROM MercariItem i GROUP BY i.sku")
    List<Object[]> getSkuStockSummary();
}