package com.example.mercari.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.mercari.entity.AuctionItem;
import com.example.mercari.repository.AuctionItemRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuctionItemControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AuctionItemRepository auctionItemRepository;

    private MockMultipartFile sampleExcelFile;

    @BeforeEach
    void setUp() throws IOException {
        // MockMvcをセットアップ
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // テストデータをクリア
        auctionItemRepository.deleteAll();
        
        // サンプルExcelファイルを作成
        byte[] excelData = createSampleExcel();
        sampleExcelFile = new MockMultipartFile(
            "file", 
            "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            excelData
        );
    }

    @Test
    void testImportExcel_Success() throws Exception {
        mockMvc.perform(multipart("/api/auction-item/import")
                .file(sampleExcelFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Excelファイルのインポートが完了しました"));

        // データベースに保存されていることを確認
        long count = auctionItemRepository.count();
        assert count == 1;
        
        AuctionItem savedItem = auctionItemRepository.findAll().get(0);
        assert "AUC001".equals(savedItem.getAuctionId());
        assert "G001".equals(savedItem.getGroupNo());
        assert "P001".equals(savedItem.getProductNo());
        assert 50000 == savedItem.getBidAmount();
        assert "PCグループ".equals(savedItem.getGroupName());
    }

    @Test
    void testImportExcel_EmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            new byte[0]
        );

        mockMvc.perform(multipart("/api/auction-item/import")
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ファイルが選択されていません"));
    }

    @Test
    void testImportExcel_InvalidFileType() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", 
            "test.csv", 
            "text/csv", 
            "test,data".getBytes()
        );

        mockMvc.perform(multipart("/api/auction-item/import")
                .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Excelファイル(.xlsx)を選択してください")));
    }

    @Test
    void testGetAuctionItemList_Empty() throws Exception {
        mockMvc.perform(get("/api/auction-item/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAuctionItemList_WithData() throws Exception {
        // テストデータを作成
        AuctionItem item = AuctionItem.builder()
            .auctionId("AUC001")
            .groupNo("G001")
            .productNo("P001")
            .bidAmount(50000)
            .groupName("PCグループ")
            .productName("ノートパソコン ThinkPad")
            .manufacturer("Lenovo")
            .build();
        auctionItemRepository.save(item);

        mockMvc.perform(get("/api/auction-item/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].auctionId", is("AUC001")))
                .andExpect(jsonPath("$[0].groupNo", is("G001")))
                .andExpect(jsonPath("$[0].productNo", is("P001")))
                .andExpect(jsonPath("$[0].bidAmount", is(50000)))
                .andExpect(jsonPath("$[0].groupName", is("PCグループ")))
                .andExpect(jsonPath("$[0].productName", is("ノートパソコン ThinkPad")))
                .andExpect(jsonPath("$[0].manufacturer", is("Lenovo")));
    }

    @Test
    void testGetAuctionItemList_WithKeywordSearch() throws Exception {
        // テストデータを作成
        AuctionItem item1 = AuctionItem.builder()
            .auctionId("AUC001")
            .groupNo("G001")
            .productNo("P001")
            .productName("ノートパソコン ThinkPad")
            .manufacturer("Lenovo")
            .build();
        
        AuctionItem item2 = AuctionItem.builder()
            .auctionId("AUC002")
            .groupNo("G002")
            .productNo("P002")
            .productName("デスクトップPC")
            .manufacturer("Dell")
            .build();
        
        auctionItemRepository.save(item1);
        auctionItemRepository.save(item2);

        // "ThinkPad"で検索
        mockMvc.perform(get("/api/auction-item/list")
                .param("keyword", "ThinkPad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productName", containsString("ThinkPad")));
    }

    @Test
    void testGetByAuctionId() throws Exception {
        // テストデータを作成
        AuctionItem item = AuctionItem.builder()
            .auctionId("AUC001")
            .groupNo("G001")
            .productNo("P001")
            .productName("ノートパソコン ThinkPad")
            .build();
        auctionItemRepository.save(item);

        mockMvc.perform(get("/api/auction-item/by-auction-id")
                .param("auctionId", "AUC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].auctionId", is("AUC001")));
    }

    @Test
    void testDownloadTemplate() throws Exception {
        mockMvc.perform(get("/api/auction-item/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", 
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", 
                    "attachment; filename=\"auction_item_template.xlsx\""));
    }

    @Test
    void testWebList() throws Exception {
        mockMvc.perform(get("/api/auction-item/web/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("auction_item_list"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    void testWebUploadForm() throws Exception {
        mockMvc.perform(get("/api/auction-item/web/upload"))
                .andExpect(status().isOk())
                .andExpect(view().name("auction_item_upload"));
    }

    /**
     * テスト用のサンプルExcelデータを作成
     */
    private byte[] createSampleExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("入札会商品");
            
            // ヘッダー行
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
            
            // データ行
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("AUC001");
            dataRow.createCell(1).setCellValue("G001");
            dataRow.createCell(2).setCellValue("P001");
            dataRow.createCell(3).setCellValue(50000);
            dataRow.createCell(4).setCellValue("PCグループ");
            dataRow.createCell(5).setCellValue(45000);
            dataRow.createCell(6).setCellValue("ノートパソコン ThinkPad");
            dataRow.createCell(7).setCellValue("動作確認済み");
            dataRow.createCell(8).setCellValue("T001");
            dataRow.createCell(9).setCellValue("Lenovo");
            dataRow.createCell(10).setCellValue("ThinkPad X1 Carbon");
            dataRow.createCell(11).setCellValue("Intel Core i7");
            dataRow.createCell(12).setCellValue("i7-10710U");
            dataRow.createCell(13).setCellValue("1.1GHz");
            dataRow.createCell(14).setCellValue("16GB");
            dataRow.createCell(15).setCellValue("512GB SSD");
            dataRow.createCell(16).setCellValue("なし");
            dataRow.createCell(17).setCellValue("あり");
            dataRow.createCell(18).setCellValue("14インチ");
            dataRow.createCell(19).setCellValue("Windows 10 Pro");
            dataRow.createCell(20).setCellValue("良好");
            dataRow.createCell(21).setCellValue("ノートPC");
            dataRow.createCell(22).setCellValue("なし");
            dataRow.createCell(23).setCellValue("ACアダプタ、マウス");
            
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}