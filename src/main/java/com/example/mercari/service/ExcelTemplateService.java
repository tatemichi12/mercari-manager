package com.example.mercari.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelTemplateService {

    /**
     * 入札会Excelテンプレートを生成
     */
    public byte[] generateAuctionItemTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("入札会商品");
            
            // ヘッダー行を作成
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "入札会ID", "グループNo.", "商品No.", "入札額", "グループ名", 
                "グループ最低入札価格", "商品名", "備考", "商品番号", "メーカー", 
                "型式", "CPU", "CPUモデル", "クロック", "メモリ", 
                "HDD", "ドライブ", "無線LAN有無", "モニタ", "COA", 
                "バッテリ状態", "タイプ", "不良・欠品内容", "付属品"
            };
            
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // サンプルデータ行を追加
            Row sampleRow = sheet.createRow(1);
            String[] sampleData = {
                "AUC001", "G001", "P001", "50000", "PCグループ",
                "45000", "ノートパソコン ThinkPad", "動作確認済み", "T001", "Lenovo",
                "ThinkPad X1 Carbon", "Intel Core i7", "i7-10710U", "1.1GHz", "16GB",
                "512GB SSD", "なし", "あり", "14インチ", "Windows 10 Pro",
                "良好", "ノートPC", "なし", "ACアダプタ、マウス"
            };
            
            for (int i = 0; i < sampleData.length; i++) {
                if (i == 3 || i == 5) { // 入札額、グループ最低入札価格は数値
                    sampleRow.createCell(i).setCellValue(Integer.parseInt(sampleData[i]));
                } else {
                    sampleRow.createCell(i).setCellValue(sampleData[i]);
                }
            }
            
            // 列幅を自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // バイト配列に変換
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}