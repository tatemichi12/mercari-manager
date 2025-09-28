package com.example.mercari.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductDescriptionServiceTest {
    
    private ProductDescriptionService service;
    
    @BeforeEach
    void setUp() {
        service = new ProductDescriptionService();
    }
    
    @Test
    void testGetIntelGenerationNumber() {
        // 10世代以上のテスト
        assertEquals(10, service.getIntelGenerationNumber("Intel Core i7-10750H"));
        assertEquals(11, service.getIntelGenerationNumber("Intel Core i5-1135G7"));
        assertEquals(12, service.getIntelGenerationNumber("Intel Core i7-12700K"));
        
        // 8-9世代のテスト
        assertEquals(8, service.getIntelGenerationNumber("Intel Core i5-8250U"));
        assertEquals(9, service.getIntelGenerationNumber("Intel Core i7-9750H"));
        
        // 7世代以下のテスト
        assertEquals(7, service.getIntelGenerationNumber("Intel Core i3-7100U"));
        assertEquals(6, service.getIntelGenerationNumber("Intel Core i5-6200U"));
        assertEquals(4, service.getIntelGenerationNumber("Intel Core i7-4770K"));
        
        // 異なる表記形式のテスト
        assertEquals(8, service.getIntelGenerationNumber("i5-8250U"));
        assertEquals(10, service.getIntelGenerationNumber("Core i7-10750H"));
        assertEquals(7, service.getIntelGenerationNumber("i3-7100U"));
        
        // 判定できないケース
        assertEquals(0, service.getIntelGenerationNumber("AMD Ryzen 5 3600"));
        assertEquals(0, service.getIntelGenerationNumber(""));
        assertEquals(0, service.getIntelGenerationNumber(null));
        assertEquals(0, service.getIntelGenerationNumber("Unknown CPU"));
    }
    
    @Test
    void testDetermineOsVersion_GML() {
        // GML + 8世代以上 → Windows 11 Pro
        assertEquals("Windows 11 Pro", service.determineOsVersion("Windows(GML)", "Intel Core i7-10750H"));
        assertEquals("Windows 11 Pro", service.determineOsVersion("Windows(GML)", "Intel Core i5-8250U"));
        assertEquals("Windows 11 Pro", service.determineOsVersion("windows(gml)", "Intel Core i7-12700K"));
        
        // GML + 7世代以下 → Windows 10 Pro
        assertEquals("Windows 10 Pro", service.determineOsVersion("Windows(GML)", "Intel Core i3-7100U"));
        assertEquals("Windows 10 Pro", service.determineOsVersion("Windows(GML)", "Intel Core i5-6200U"));
    }
    
    @Test
    void testDetermineOsVersion_NonGML() {
        // 非GML + 8世代以上 → Windows 11 Home
        assertEquals("Windows 11 Home", service.determineOsVersion("Windows", "Intel Core i7-10750H"));
        assertEquals("Windows 11 Home", service.determineOsVersion("Windows", "Intel Core i5-8250U"));
        assertEquals("Windows 11 Home", service.determineOsVersion("", "Intel Core i7-12700K"));
        
        // 非GML + 7世代以下 → Windows 10 Pro
        assertEquals("Windows 10 Pro", service.determineOsVersion("Windows", "Intel Core i3-7100U"));
        assertEquals("Windows 10 Pro", service.determineOsVersion("Windows", "Intel Core i5-6200U"));
        assertEquals("Windows 10 Pro", service.determineOsVersion(null, "Intel Core i5-6200U"));
    }
    
    @Test
    void testBuildDescription() {
        String baseDescription = "高性能ノートPCです。";
        String result = service.buildDescription(baseDescription, "Windows(GML)", "Intel Core i7-10750H", "ACアダプター付属");
        
        assertTrue(result.contains("高性能ノートPCです。"));
        assertTrue(result.contains("OS: Windows 11 Pro"));
        assertTrue(result.contains("CPU: Intel Core i7-10750H"));
        assertTrue(result.contains("Windows 11 Proインストール済み"));
        assertTrue(result.contains("ACアダ"));
    }
    
    @Test
    void testBuildDescription_Windows10Pro() {
        String baseDescription = "コンパクトデスクトップPC。";
        String result = service.buildDescription(baseDescription, "Windows(GML)", "Intel Core i5-6200U", null);
        
        assertTrue(result.contains("OS: Windows 10 Pro"));
        assertTrue(result.contains("Windows 10 Proインストール済み"));
        assertFalse(result.contains("ACアダプター"));
    }
    
    @Test
    void testBuildDescription_Windows11Home() {
        String baseDescription = "ゲーミングPC。";
        String result = service.buildDescription(baseDescription, "Windows", "Intel Core i7-12700K", "電源ケーブル付属");
        
        assertTrue(result.contains("OS: Windows 11 Home"));
        assertTrue(result.contains("Windows 11 Homeインストール済み"));
        assertTrue(result.contains("電源ケーブル付属"));
    }
    
    @Test
    void testUpdateOsInDescription() {
        String existingDescription = "高性能PC\n\n【スペック】\nOS: Windows 10 Home\nCPU: Intel Core i7-10750H\n\n【インストール済み】\nWindows 10 Homeインストール済み";
        
        String result = service.updateOsInDescription(existingDescription, "Windows(GML)", "Intel Core i7-10750H");
        
        assertTrue(result.contains("OS: Windows 11 Pro"));
        assertTrue(result.contains("Windows 11 Proインストール済み"));
        assertFalse(result.contains("Windows 10 Home"));
    }
    
    @Test
    void testEdgeCases() {
        // 空の説明文
        String result = service.buildDescription("", "Windows(GML)", "Intel Core i7-10750H", null);
        assertTrue(result.contains("OS: Windows 11 Pro"));
        
        // null CPU名
        result = service.buildDescription("テスト", "Windows(GML)", null, null);
        assertTrue(result.contains("CPU: Intel CPU"));
        
        // 未知のCPU（世代判定不可）
        assertEquals("Windows 10 Pro", service.determineOsVersion("Windows(GML)", "AMD Ryzen 5 3600"));
    }
}