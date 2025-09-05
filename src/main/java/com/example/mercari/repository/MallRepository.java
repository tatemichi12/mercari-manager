package com.example.mercari.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mercari.entity.Mall;

public interface MallRepository extends JpaRepository<Mall, Long> {
        Mall findByCode(String code);
    // 必要なら追加メソッド
}