package com.example.mercari.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.mercari.entity.MercariItem; // MercariItemに修正
import com.example.mercari.repository.MercariItemRepository; // MercariItemRepositoryに修正
import com.opencsv.CSVReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final MercariItemRepository mercariItemRepository;

    public void importFromMultipartFile(MultipartFile file) {
        try (
            Reader reader = new InputStreamReader(file.getInputStream(), Charset.forName("MS932")); // Excel互換
            CSVReader csvReader = new CSVReader(reader)
        ) {
            List<String[]> records = csvReader.readAll();
            if (records.isEmpty()) return;

            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                if (row.length <= 153) continue; // 必要な列数が足りない場合スキップ

                String productId = nullSafe(row, 0); // 重複判定キー

                Optional<MercariItem> existingOpt = mercariItemRepository.findByProductId(productId);

                MercariItem item = existingOpt.orElse(MercariItem.builder().build());

                // 新規 or 上書きどちらでもセットする内容
                item.setProductId(productId);
                item.setSnapshotId(nullSafe(row, 1));
                item.setImageUpdateFlag1(nullSafe(row, 3));
                item.setImageUpdateFlag2(nullSafe(row, 6));
                item.setImageUpdateFlag3(nullSafe(row, 9));
                item.setImageUpdateFlag4(nullSafe(row, 12));
                item.setImageUpdateFlag5(nullSafe(row, 15));
                item.setImageUpdateFlag6(nullSafe(row, 18));
                item.setImageUpdateFlag7(nullSafe(row, 21));
                item.setImageUpdateFlag8(nullSafe(row, 24));
                item.setImageUpdateFlag9(nullSafe(row, 27));
                item.setImageUpdateFlag10(nullSafe(row, 30));
                item.setImageUpdateFlag11(nullSafe(row, 33));
                item.setImageUpdateFlag12(nullSafe(row, 36));
                item.setImageUpdateFlag13(nullSafe(row, 39));
                item.setImageUpdateFlag14(nullSafe(row, 42));
                item.setImageUpdateFlag15(nullSafe(row, 45));
                item.setImageUpdateFlag16(nullSafe(row, 48));
                item.setImageUpdateFlag17(nullSafe(row, 51));
                item.setImageUpdateFlag18(nullSafe(row, 54));
                item.setImageUpdateFlag19(nullSafe(row, 57));
                item.setImageUpdateFlag20(nullSafe(row, 60));
                item.setHash(nullSafe(row, 161));
                item.setTitle(nullSafe(row, 62));
                item.setDescription(nullSafe(row, 63));
                item.setSkuCode(nullSafe(row, 64));
                item.setStock(parseIntSafe(nullSafe(row, 67)));
                item.setSku(nullSafe(row, 70));
                item.setJanCode(nullSafe(row, 71));
                item.setPrice(parseIntSafe(nullSafe(row, 145)));
                item.setCategoryId(nullSafe(row, 146));
                item.setCondition(nullSafe(row, 147));
                item.setShippingMethod(parseIntSafe(nullSafe(row, 148)));
                item.setRegionCode(nullSafe(row, 149));
                item.setShippingDays(parseIntSafe(nullSafe(row, 150)));
                item.setShippingCharge(parseIntSafe(nullSafe(row, 151)));
                item.setShippingFeeId(nullSafe(row, 152));
                item.setStatus(parseIntSafe(nullSafe(row, 153)));
                item.setBrandId(nullSafe(row, 144)); // brandIdはString型

                // 新規登録のときのみ初期値を設定
                if (!existingOpt.isPresent()) {
                    item.setCost(0); // コストがCSVに無い場合は初期値0
                }

                mercariItemRepository.save(item);
            }

        } catch (Exception e) {
            throw new RuntimeException("CSVアップロードに失敗しました", e);
        }
    }

    // 空欄またはnullを安全に数値に変換（失敗時は0）
    private Integer parseIntSafe(String value) {
        try {
            return (value == null || value.trim().isEmpty()) ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 空欄安全取得（配列アクセスとnull防止）
    private String nullSafe(String[] row, int index) {
        return (index < row.length && row[index] != null) ? row[index].trim() : "";
    }
}