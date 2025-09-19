package com.example.mercari.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.mercari.entity.AuctionItem;
import com.example.mercari.service.AuctionItemService;
import com.example.mercari.service.ExcelTemplateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auction-item")
@Slf4j
public class AuctionItemController {

    private final AuctionItemService auctionItemService;
    private final ExcelTemplateService excelTemplateService;

    /**
     * 入札会商品一覧取得API
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<List<AuctionItem>> getAuctionItemList(
        @RequestParam(value = "keyword", required = false) String keyword) {
        
        try {
            List<AuctionItem> items;
            if (keyword != null && !keyword.trim().isEmpty()) {
                items = auctionItemService.searchAll(keyword.trim());
            } else {
                items = auctionItemService.findAll();
            }
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("入札会商品一覧取得でエラーが発生しました", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Excelインポート処理API
     */
    @PostMapping("/import")
    @ResponseBody
    public ResponseEntity<String> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("ファイルが選択されていません");
            }

            auctionItemService.importFromExcel(file);
            return ResponseEntity.ok("Excelファイルのインポートが完了しました");
            
        } catch (RuntimeException e) {
            log.error("Excelインポートでエラーが発生しました", e);
            return ResponseEntity.badRequest().body("エラー: " + e.getMessage());
        } catch (Exception e) {
            log.error("予期しないエラーが発生しました", e);
            return ResponseEntity.internalServerError().body("システムエラーが発生しました");
        }
    }

    /**
     * Web UI用の入札会商品一覧ページ
     */
    @GetMapping("/web/list")
    public String webList(
        @RequestParam(value = "keyword", required = false) String keyword,
        Model model) {
        
        try {
            List<AuctionItem> items;
            if (keyword != null && !keyword.trim().isEmpty()) {
                items = auctionItemService.searchAll(keyword.trim());
                model.addAttribute("keyword", keyword.trim());
            } else {
                items = auctionItemService.findAll();
            }
            
            model.addAttribute("items", items);
            return "auction_item_list";
            
        } catch (Exception e) {
            log.error("入札会商品一覧表示でエラーが発生しました", e);
            model.addAttribute("error", "データの取得に失敗しました: " + e.getMessage());
            return "auction_item_list";
        }
    }

    /**
     * Web UI用のExcelアップロードフォーム
     */
    @GetMapping("/web/upload")
    public String webUploadForm() {
        return "auction_item_upload";
    }

    /**
     * Web UI用のExcelアップロード処理
     */
    @PostMapping("/web/upload")
    public String webUpload(
        @RequestParam("file") MultipartFile file,
        RedirectAttributes redirectAttributes) {
        
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "ファイルが選択されていません");
                return "redirect:/api/auction-item/web/upload";
            }

            auctionItemService.importFromExcel(file);
            redirectAttributes.addFlashAttribute("success", "Excelファイルのインポートが完了しました");
            return "redirect:/api/auction-item/web/list";
            
        } catch (RuntimeException e) {
            log.error("Excelインポートでエラーが発生しました", e);
            redirectAttributes.addFlashAttribute("error", "インポートエラー: " + e.getMessage());
            return "redirect:/api/auction-item/web/upload";
        } catch (Exception e) {
            log.error("予期しないエラーが発生しました", e);
            redirectAttributes.addFlashAttribute("error", "システムエラーが発生しました");
            return "redirect:/api/auction-item/web/upload";
        }
    }

    /**
     * 入札会IDでの検索API
     */
    @GetMapping("/by-auction-id")
    @ResponseBody
    public ResponseEntity<List<AuctionItem>> getByAuctionId(
        @RequestParam("auctionId") String auctionId) {
        
        try {
            List<AuctionItem> items = auctionItemService.findByAuctionId(auctionId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("入札会ID検索でエラーが発生しました", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Excelテンプレートダウンロード
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            byte[] template = excelTemplateService.generateAuctionItemTemplate();
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"auction_item_template.xlsx\"");
            response.setContentLength(template.length);
            
            response.getOutputStream().write(template);
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            log.error("テンプレートダウンロードでエラーが発生しました", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}