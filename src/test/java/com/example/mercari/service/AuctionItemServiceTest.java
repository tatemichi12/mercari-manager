package com.example.mercari.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.mercari.entity.AuctionItem;
import com.example.mercari.repository.AuctionItemRepository;

@ExtendWith(MockitoExtension.class)
class AuctionItemServiceTest {

    @Mock
    private AuctionItemRepository auctionItemRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AuctionItemService auctionItemService;

    private AuctionItem sampleAuctionItem;

    @BeforeEach
    void setUp() {
        sampleAuctionItem = AuctionItem.builder()
            .auctionId("AUC001")
            .groupNo("G001")
            .productNo("P001")
            .bidAmount(50000)
            .groupName("PCグループ")
            .groupMinBidPrice(45000)
            .productName("ノートパソコン ThinkPad")
            .manufacturer("Lenovo")
            .model("ThinkPad X1 Carbon")
            .build();
    }

    @Test
    void testFindAll() {
        // Given
        List<AuctionItem> expectedItems = List.of(sampleAuctionItem);
        when(auctionItemRepository.findAll()).thenReturn(expectedItems);

        // When
        List<AuctionItem> result = auctionItemService.findAll();

        // Then
        assertEquals(expectedItems, result);
        verify(auctionItemRepository).findAll();
    }

    @Test
    void testSearchAll() {
        // Given
        String keyword = "ThinkPad";
        List<AuctionItem> expectedItems = List.of(sampleAuctionItem);
        when(auctionItemRepository.searchAll(keyword)).thenReturn(expectedItems);

        // When
        List<AuctionItem> result = auctionItemService.searchAll(keyword);

        // Then
        assertEquals(expectedItems, result);
        verify(auctionItemRepository).searchAll(keyword);
    }

    @Test
    void testFindByAuctionId() {
        // Given
        String auctionId = "AUC001";
        List<AuctionItem> expectedItems = List.of(sampleAuctionItem);
        when(auctionItemRepository.findByAuctionId(auctionId)).thenReturn(expectedItems);

        // When
        List<AuctionItem> result = auctionItemService.findByAuctionId(auctionId);

        // Then
        assertEquals(expectedItems, result);
        verify(auctionItemRepository).findByAuctionId(auctionId);
    }

    @Test
    void testImportFromExcel_EmptyFile() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auctionItemService.importFromExcel(multipartFile);
        });
        assertEquals("ファイルが選択されていません", exception.getMessage());
    }

    @Test
    void testImportFromExcel_InvalidFileExtension() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.csv");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auctionItemService.importFromExcel(multipartFile);
        });
        assertEquals("Excelファイル(.xlsx)を選択してください", exception.getMessage());
    }

    @Test
    void testImportFromExcel_NewItem() throws IOException {
        // Given
        byte[] excelData = createSampleExcel();
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(excelData));
        when(auctionItemRepository.findByAuctionIdAndGroupNoAndProductNo(anyString(), anyString(), anyString()))
            .thenReturn(Optional.empty());

        // When
        auctionItemService.importFromExcel(multipartFile);

        // Then
        verify(auctionItemRepository).save(any(AuctionItem.class));
    }

    @Test
    void testImportFromExcel_UpdateExistingItem() throws IOException {
        // Given
        byte[] excelData = createSampleExcel();
        AuctionItem existingItem = AuctionItem.builder()
            .id(1L)
            .auctionId("AUC001")
            .groupNo("G001")
            .productNo("P001")
            .build();

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(excelData));
        when(auctionItemRepository.findByAuctionIdAndGroupNoAndProductNo(anyString(), anyString(), anyString()))
            .thenReturn(Optional.of(existingItem));

        // When
        auctionItemService.importFromExcel(multipartFile);

        // Then
        verify(auctionItemRepository).save(existingItem);
        assertEquals(50000, existingItem.getBidAmount());
        assertEquals("PCグループ", existingItem.getGroupName());
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