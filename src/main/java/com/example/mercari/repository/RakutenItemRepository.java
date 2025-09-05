package com.example.mercari.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mercari.entity.RakutenItem;

@Repository
public interface RakutenItemRepository extends JpaRepository<RakutenItem, Long> {
    // 必要に応じて検索メソッド追加
}