package com.example.mercari.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.mercari.entity.RakutenItem;
import com.example.mercari.repository.RakutenItemRepository;
import com.opencsv.CSVReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RakutenCsvImportService {

    private final RakutenItemRepository rakutenItemRepository;

    public void importFromMultipartFile(MultipartFile file) {
        try (
            Reader reader = new InputStreamReader(file.getInputStream(), Charset.forName("MS932"));
            CSVReader csvReader = new CSVReader(reader)
        ) {
            List<String[]> records = csvReader.readAll();
            if (records.isEmpty()) return;

            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);

                RakutenItem item = RakutenItem.builder()
                        .flag(nullSafe(row, 0))
                        .title(nullSafe(row, 1)) // 継承フィールド (商品名)
                        .titleMobile(nullSafe(row, 2))
                        .itemNumber(nullSafe(row, 3))
                        .itemId(nullSafe(row, 4))
                        .pageId(nullSafe(row, 5))
                        .price(parseIntSafe(nullSafe(row,6))) // 継承フィールド (実売価格)
                        .displayPrice(parseIntSafe(nullSafe(row,7)))
                        .taxFlag(parseIntSafe(nullSafe(row,8)))
                        .shippingFlag(parseIntSafe(nullSafe(row,9)))
                        .individualShipping(parseIntSafe(nullSafe(row,10)))
                        .orderButton(parseIntSafe(nullSafe(row,11)))
                        .documentRequestButton(parseIntSafe(nullSafe(row,12)))
                        .inquiryButton(parseIntSafe(nullSafe(row,13)))
                        .recommendButton(parseIntSafe(nullSafe(row,14)))
                        .noshiFlag(parseIntSafe(nullSafe(row,15)))
                        .rakutenStock(parseIntSafe(nullSafe(row,16))) // 楽天専用在庫
                        .options(nullSafe(row,17))
                        .limitedSaleStart(nullSafe(row,18))
                        .limitedSaleEnd(nullSafe(row,19))
                        .rakutenDescription(nullSafe(row,20))
                        .rakutenImageName(nullSafe(row,21))
                        .directoryId(nullSafe(row,22))
                        .mobileFlag(parseIntSafe(nullSafe(row,23)))
                        .build();

                // 共通在庫数もセット
                item.setStock(item.getRakutenStock());

                rakutenItemRepository.save(item);
            }
        } catch (Exception e) {
            throw new RuntimeException("楽天CSVアップロードに失敗しました", e);
        }
    }

    private Integer parseIntSafe(String value) {
        try {
            return (value == null || value.trim().isEmpty()) ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String nullSafe(String[] row, int index) {
        return (index < row.length && row[index] != null) ? row[index].trim() : "";
    }
}