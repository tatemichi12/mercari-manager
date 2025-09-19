package com.example.mercari.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.mercari.entity.AuctionItem;
import com.example.mercari.repository.AuctionItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionItemService {

    private final AuctionItemRepository auctionItemRepository;

    /**
     * 全ての入札会商品を取得
     */
    public List<AuctionItem> findAll() {
        return auctionItemRepository.findAll();
    }

    /**
     * キーワード検索
     */
    public List<AuctionItem> searchAll(String keyword) {
        return auctionItemRepository.searchAll(keyword);
    }

    /**
     * 入札会IDで検索
     */
    public List<AuctionItem> findByAuctionId(String auctionId) {
        return auctionItemRepository.findByAuctionId(auctionId);
    }

    /**
     * ExcelファイルをインポートしてDBに保存
     */
    @Transactional
    public void importFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("ファイルが選択されていません");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            throw new RuntimeException("Excelファイル(.xlsx)を選択してください");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // ヘッダー行をスキップして2行目から処理
            int importCount = 0;
            int updateCount = 0;
            
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                
                try {
                    AuctionItem item = parseRowToAuctionItem(row);
                    if (item == null) continue; // 必須項目が不足している場合はスキップ
                    
                    // 重複チェック（入札会ID + グループNo. + 商品No.）
                    Optional<AuctionItem> existingOpt = auctionItemRepository
                        .findByAuctionIdAndGroupNoAndProductNo(
                            item.getAuctionId(), 
                            item.getGroupNo(), 
                            item.getProductNo()
                        );
                    
                    if (existingOpt.isPresent()) {
                        // 既存レコードを更新
                        AuctionItem existing = existingOpt.get();
                        updateAuctionItem(existing, item);
                        auctionItemRepository.save(existing);
                        updateCount++;
                    } else {
                        // 新規レコードを保存
                        auctionItemRepository.save(item);
                        importCount++;
                    }
                    
                } catch (Exception e) {
                    log.warn("行{}の処理でエラーが発生しました: {}", rowIndex + 1, e.getMessage());
                    // エラー行をスキップして処理続行
                }
            }
            
            log.info("Excelインポート完了: 新規{}件, 更新{}件", importCount, updateCount);
            
        } catch (IOException e) {
            throw new RuntimeException("Excelファイルの読み込みに失敗しました", e);
        }
    }

    /**
     * Excel行をAuctionItemエンティティに変換
     */
    private AuctionItem parseRowToAuctionItem(Row row) {
        // 必須項目のチェック（入札会ID、グループNo.、商品No.）
        String auctionId = getCellValueAsString(row, 0);
        String groupNo = getCellValueAsString(row, 1);
        String productNo = getCellValueAsString(row, 2);
        
        if (auctionId.isEmpty() || groupNo.isEmpty() || productNo.isEmpty()) {
            return null; // 必須項目が不足している場合はスキップ
        }

        return AuctionItem.builder()
            .auctionId(auctionId)                               // 1. 入札会ID
            .groupNo(groupNo)                                   // 2. グループNo.
            .productNo(productNo)                               // 3. 商品No.
            .bidAmount(getCellValueAsInteger(row, 3))           // 4. 入札額
            .groupName(getCellValueAsString(row, 4))            // 5. グループ名
            .groupMinBidPrice(getCellValueAsInteger(row, 5))    // 6. グループ最低入札価格
            .productName(getCellValueAsString(row, 6))          // 7. 商品名
            .remarks(getCellValueAsString(row, 7))              // 8. 備考
            .itemNumber(getCellValueAsString(row, 8))           // 9. 商品番号
            .manufacturer(getCellValueAsString(row, 9))         // 10. メーカー
            .model(getCellValueAsString(row, 10))               // 11. 型式
            .cpu(getCellValueAsString(row, 11))                 // 12. CPU
            .cpuModel(getCellValueAsString(row, 12))            // 13. CPUモデル
            .clock(getCellValueAsString(row, 13))               // 14. クロック
            .memory(getCellValueAsString(row, 14))              // 15. メモリ
            .hdd(getCellValueAsString(row, 15))                 // 16. HDD
            .drive(getCellValueAsString(row, 16))               // 17. ドライブ
            .wirelessLan(getCellValueAsString(row, 17))         // 18. 無線LAN有無
            .monitor(getCellValueAsString(row, 18))             // 19. モニタ
            .coa(getCellValueAsString(row, 19))                 // 20. COA
            .batteryCondition(getCellValueAsString(row, 20))    // 21. バッテリ状態
            .type(getCellValueAsString(row, 21))                // 22. タイプ
            .defectContent(getCellValueAsString(row, 22))       // 23. 不良・欠品内容
            .accessories(getCellValueAsString(row, 23))         // 24. 付属品
            .build();
    }

    /**
     * 既存のAuctionItemを新しいデータで更新
     */
    private void updateAuctionItem(AuctionItem existing, AuctionItem newItem) {
        existing.setBidAmount(newItem.getBidAmount());
        existing.setGroupName(newItem.getGroupName());
        existing.setGroupMinBidPrice(newItem.getGroupMinBidPrice());
        existing.setProductName(newItem.getProductName());
        existing.setRemarks(newItem.getRemarks());
        existing.setItemNumber(newItem.getItemNumber());
        existing.setManufacturer(newItem.getManufacturer());
        existing.setModel(newItem.getModel());
        existing.setCpu(newItem.getCpu());
        existing.setCpuModel(newItem.getCpuModel());
        existing.setClock(newItem.getClock());
        existing.setMemory(newItem.getMemory());
        existing.setHdd(newItem.getHdd());
        existing.setDrive(newItem.getDrive());
        existing.setWirelessLan(newItem.getWirelessLan());
        existing.setMonitor(newItem.getMonitor());
        existing.setCoa(newItem.getCoa());
        existing.setBatteryCondition(newItem.getBatteryCondition());
        existing.setType(newItem.getType());
        existing.setDefectContent(newItem.getDefectContent());
        existing.setAccessories(newItem.getAccessories());
    }

    /**
     * セルの値を文字列として取得（安全）
     */
    private String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 数値を文字列に変換（小数点以下を除去）
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * セルの値を整数として取得（安全）
     */
    private Integer getCellValueAsInteger(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                String stringValue = cell.getStringCellValue().trim();
                if (stringValue.isEmpty()) return null;
                try {
                    return Integer.parseInt(stringValue.replaceAll(",", ""));
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}