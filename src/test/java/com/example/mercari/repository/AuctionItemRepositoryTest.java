package com.example.mercari.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.mercari.entity.AuctionItem;

@DataJpaTest
@ActiveProfiles("test")
class AuctionItemRepositoryTest {

    @Autowired
    private AuctionItemRepository auctionItemRepository;

    private AuctionItem sampleItem1;
    private AuctionItem sampleItem2;

    @BeforeEach
    void setUp() {
        auctionItemRepository.deleteAll();
        
        sampleItem1 = AuctionItem.builder()
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

        sampleItem2 = AuctionItem.builder()
            .auctionId("AUC001")
            .groupNo("G002") 
            .productNo("P002")
            .bidAmount(30000)
            .groupName("モニターグループ")
            .productName("液晶モニター")
            .manufacturer("Dell")
            .model("U2720Q")
            .build();

        auctionItemRepository.save(sampleItem1);
        auctionItemRepository.save(sampleItem2);
    }

    @Test
    void testFindByAuctionIdAndGroupNoAndProductNo() {
        Optional<AuctionItem> found = auctionItemRepository
            .findByAuctionIdAndGroupNoAndProductNo("AUC001", "G001", "P001");
        
        assertTrue(found.isPresent());
        assertEquals("ノートパソコン ThinkPad", found.get().getProductName());
        assertEquals("Lenovo", found.get().getManufacturer());
    }

    @Test
    void testFindByAuctionId() {
        List<AuctionItem> items = auctionItemRepository.findByAuctionId("AUC001");
        
        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(item -> "G001".equals(item.getGroupNo())));
        assertTrue(items.stream().anyMatch(item -> "G002".equals(item.getGroupNo())));
    }

    @Test
    void testFindByGroupNo() {
        List<AuctionItem> items = auctionItemRepository.findByGroupNo("G001");
        
        assertEquals(1, items.size());
        assertEquals("ノートパソコン ThinkPad", items.get(0).getProductName());
    }

    @Test
    void testFindByProductNameContaining() {
        List<AuctionItem> items = auctionItemRepository.findByProductNameContaining("ThinkPad");
        
        assertEquals(1, items.size());
        assertEquals("Lenovo", items.get(0).getManufacturer());
    }

    @Test
    void testFindByManufacturer() {
        List<AuctionItem> items = auctionItemRepository.findByManufacturer("Lenovo");
        
        assertEquals(1, items.size());
        assertEquals("ノートパソコン ThinkPad", items.get(0).getProductName());
    }

    @Test
    void testSearchAll() {
        // "ThinkPad"で検索
        List<AuctionItem> items1 = auctionItemRepository.searchAll("ThinkPad");
        assertEquals(1, items1.size());
        assertEquals("Lenovo", items1.get(0).getManufacturer());

        // "AUC001"で検索（入札会IDでの検索）
        List<AuctionItem> items2 = auctionItemRepository.searchAll("AUC001");
        assertEquals(2, items2.size());

        // "Dell"で検索（メーカーでの検索）
        List<AuctionItem> items3 = auctionItemRepository.searchAll("Dell");
        assertEquals(1, items3.size());
        assertEquals("液晶モニター", items3.get(0).getProductName());
    }

    @Test
    void testSearchAll_NoResults() {
        List<AuctionItem> items = auctionItemRepository.searchAll("存在しないキーワード");
        assertTrue(items.isEmpty());
    }

    @Test
    void testSearchAll_EmptyKeyword() {
        List<AuctionItem> items = auctionItemRepository.searchAll("");
        assertEquals(2, items.size());
    }

    @Test
    void testSearchAll_NullKeyword() {
        List<AuctionItem> items = auctionItemRepository.searchAll(null);
        assertEquals(2, items.size());
    }
}