package com.example.mercari.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.mercari.entity.Mall;
import com.example.mercari.entity.MercariItem;
import com.example.mercari.form.ItemListForm;
import com.example.mercari.repository.MallRepository;
import com.example.mercari.repository.MercariItemRepository;
import com.example.mercari.service.CsvImportService;
import com.opencsv.CSVWriter;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final MercariItemRepository mercariItemRepository;
    private final CsvImportService csvImportService;
    private final MallRepository mallRepository; // 

    // 商品一覧
    @GetMapping
    public String listItems(
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "status", required = false) Integer status,
        Model model) {

        List<MercariItem> items;

        if (status != null) {
            if (keyword != null && !keyword.isEmpty()) {
                items = mercariItemRepository.searchAllByStatus(status, keyword);
            } else {
                items = mercariItemRepository.findByStatus(status);
            }
        } else { // 全部
            if (keyword != null && !keyword.isEmpty()) {
                items = mercariItemRepository.searchAll(keyword);
            } else {
                items = mercariItemRepository.findAll();
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "item_list";
    }
    // 登録フォーム
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("item", new MercariItem());
        return "item_form";
    }

    // 編集フォーム
    @GetMapping("/edit/{id}")
    public String editItem(@PathVariable Long id, Model model) {
        MercariItem item = mercariItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無効なID: " + id));
        model.addAttribute("item", item);
        return "item_form";
    }

    // 保存（新規・編集）
    @PostMapping("/save")
    public String saveItem(@ModelAttribute("item") MercariItem item, BindingResult result, Model model) {
        // バリデーション
        if (result.hasErrors()) {
            model.addAttribute("item", item);
            return "item_form";
        }

        // 更新時はcreatedAtを維持
        if (item.getId() != null) {
            Optional<MercariItem> existingOpt = mercariItemRepository.findById(item.getId());
            if (existingOpt.isPresent()) {
                item.setCreatedAt(existingOpt.get().getCreatedAt());
            }
        }
        item.setUpdatedAt(java.time.LocalDateTime.now());

        mercariItemRepository.save(item);
        return "redirect:/items";
    }

    // 詳細
    @GetMapping("/{id}")
    public String viewItem(@PathVariable Long id, Model model) {
        MercariItem item = mercariItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無効なID: " + id));
        model.addAttribute("item", item);
        return "item_detail";
    }

    // 削除
    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        mercariItemRepository.deleteById(id);
        return "redirect:/items";
    }

    // CSVアップロードフォーム
    @GetMapping("/upload")
    public String showUploadForm() {
        return "item_upload";
    }

    // CSVアップロード
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            csvImportService.importFromMultipartFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/items";
    }

    // バッチアクション
    @PostMapping("/batch-action")
    @Transactional
    public String handleBatchAction(
        @RequestParam(name = "selectedIds", required = false) List<Long> selectedIds,
        @RequestParam("action") String action,
        @RequestParam(name = "shippingDaysValue", required = false) Integer shippingDaysValue,
        @ModelAttribute ItemListForm itemListForm,
        @RequestParam(name = "appendText", required = false) String appendText,
        @RequestParam(name = "discountAmount", required = false) Integer discountAmount, // ←追加
        HttpServletResponse response,
        Model model
    ) {
        List<MercariItem> formItems = itemListForm.getItems();

        if (selectedIds == null || selectedIds.isEmpty()) {
            model.addAttribute("error", "商品が選択されていません。");
            return "redirect:/items";
        }

        switch (action) {
            case "delete":
                mercariItemRepository.deleteAllById(selectedIds);
                break;
            case "csv":
                return exportCsv(selectedIds, response);
            case "shippingDays_update":
                if (shippingDaysValue != null) {
                    mercariItemRepository.updateShippingDaysForIds(shippingDaysValue, selectedIds);
                }
                break;
            case "append_description":
                if (appendText != null && !appendText.trim().isEmpty()) {
                    List<MercariItem> items = mercariItemRepository.findAllById(selectedIds);
                    for (MercariItem item : items) {
                        String currentDesc = item.getDescription() != null ? item.getDescription() : "";
                        item.setDescription(currentDesc + "\n" + appendText);
                    }
                    mercariItemRepository.saveAll(items);
                }
                break;
            case "save_description":
                List<MercariItem> itemsToUpdate = mercariItemRepository.findAllById(selectedIds);
                for (MercariItem dbItem : itemsToUpdate) {
                    for (MercariItem formItem : formItems) {
                        if (dbItem.getId().equals(formItem.getId())) {
                            dbItem.setDescription(formItem.getDescription());
                            break;
                        }
                    }
                }
                mercariItemRepository.saveAll(itemsToUpdate);
                break;
            case "batch_save":
                List<MercariItem> batchItemsToUpdate = mercariItemRepository.findAllById(selectedIds);
                for (MercariItem dbItem : batchItemsToUpdate) {
                    for (MercariItem formItem : formItems) {
                        if (dbItem.getId().equals(formItem.getId())) {
                            dbItem.setTitle(formItem.getTitle());
                            dbItem.setPrice(formItem.getPrice());
                            dbItem.setStatus(formItem.getStatus());
                            dbItem.setSku(formItem.getSku());
                            dbItem.setStock(formItem.getStock());
                            dbItem.setShippingDays(formItem.getShippingDays());
                            dbItem.setDescription(formItem.getDescription());
                            // 必要なら他の項目も
                            break;
                        }
                    }
                }
                mercariItemRepository.saveAll(batchItemsToUpdate);
                break;

            case "batch_discount":
                // 値下げ額取得（null安全、100円未満は無効）
                if (discountAmount == null || discountAmount < 100) discountAmount = 100;

                List<MercariItem> discountItems = mercariItemRepository.findAllById(selectedIds);
                for (MercariItem dbItem : discountItems) {
                    if (dbItem.getPrice() != null && dbItem.getPrice() >= discountAmount) {
                        dbItem.setPrice(dbItem.getPrice() - discountAmount);
                    }
                }
                mercariItemRepository.saveAll(discountItems);
                break;

            // ... 他のcase ...
        }
        return "redirect:/items";
    }

    private String exportCsv(List<Long> selectedIds, HttpServletResponse response) {
        try {
            List<MercariItem> items = mercariItemRepository.findAllById(selectedIds);
            String fileName = URLEncoder.encode("selected_items.csv", StandardCharsets.UTF_8);
            response.setContentType("text/csv; charset=MS932");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "MS932");
                 CSVWriter csvWriter = new CSVWriter(writer)) {

                String[] header = {
                    "ID", "商品ID", "スナップショットID",
                    "商品画像更新フラグ_1", "商品画像更新フラグ_2", "商品画像更新フラグ_3", "商品画像更新フラグ_4",
                    "商品画像更新フラグ_5", "商品画像更新フラグ_6", "商品画像更新フラグ_7", "商品画像更新フラグ_8",
                    "商品画像更新フラグ_9", "商品画像更新フラグ_10", "商品画像更新フラグ_11", "商品画像更新フラグ_12",
                    "商品画像更新フラグ_13", "商品画像更新フラグ_14", "商品画像更新フラグ_15", "商品画像更新フラグ_16",
                    "商品画像更新フラグ_17", "商品画像更新フラグ_18", "商品画像更新フラグ_19", "商品画像更新フラグ_20",
                    "SKU1_ID",
                    "Hash", "商品名", "販売価格", "仕入れ価格", "商品説明", "SKU1_商品管理コード",
                    "商品画像名", "画像2", "画像3", "画像4", "画像5",
                    "SKUタイプ1", "SKU1_在庫数", "商品管理コード", "SKU1_JANコード",
                    "ブランドID", "カテゴリID", "商品の状態",
                    "配送方法", "発送元の地域", "発送までの日数", "商品ステータス",
                    "配送料の負担", "送料ID", "登録日", "更新日"
                };
                csvWriter.writeNext(header);

                for (MercariItem item : items) {
                    String[] data = {
                        toStringSafe(item.getId()),
                        toStringSafe(item.getProductId()),
                        nullToEmpty(item.getSnapshotId()),
                        nullToEmpty(item.getImageUpdateFlag1()),
                        nullToEmpty(item.getImageUpdateFlag2()),
                        nullToEmpty(item.getImageUpdateFlag3()),
                        nullToEmpty(item.getImageUpdateFlag4()),
                        nullToEmpty(item.getImageUpdateFlag5()),
                        nullToEmpty(item.getImageUpdateFlag6()),
                        nullToEmpty(item.getImageUpdateFlag7()),
                        nullToEmpty(item.getImageUpdateFlag8()),
                        nullToEmpty(item.getImageUpdateFlag9()),
                        nullToEmpty(item.getImageUpdateFlag10()),
                        nullToEmpty(item.getImageUpdateFlag11()),
                        nullToEmpty(item.getImageUpdateFlag12()),
                        nullToEmpty(item.getImageUpdateFlag13()),
                        nullToEmpty(item.getImageUpdateFlag14()),
                        nullToEmpty(item.getImageUpdateFlag15()),
                        nullToEmpty(item.getImageUpdateFlag16()),
                        nullToEmpty(item.getImageUpdateFlag17()),
                        nullToEmpty(item.getImageUpdateFlag18()),
                        nullToEmpty(item.getImageUpdateFlag19()),
                        nullToEmpty(item.getImageUpdateFlag20()),
                        nullToEmpty(item.getSkuCode()),
                        nullToEmpty(item.getHash()),
                        nullToEmpty(item.getTitle()),
                        toStringSafe(item.getPrice()),
                        toStringSafe(item.getCost()),
                        nullToEmpty(item.getDescription()),
                        nullToEmpty(item.getSku()),
                        nullToEmpty(item.getImage1()),
                        nullToEmpty(item.getImage2()),
                        nullToEmpty(item.getImage3()),
                        nullToEmpty(item.getImage4()),
                        nullToEmpty(item.getImage5()),
                        nullToEmpty(item.getSkuType()),
                        toStringSafe(item.getStock()),
                        nullToEmpty(item.getSkuCode()),
                        nullToEmpty(item.getJanCode()),
                        nullToEmpty(item.getBrandId()),
                        nullToEmpty(item.getCategoryId()),
                        nullToEmpty(item.getCondition()),
                        toStringSafe(item.getShippingMethod()),
                        nullToEmpty(item.getRegionCode()),
                        toStringSafe(item.getShippingDays()),
                        toStringSafe(item.getStatus()),
                        toStringSafe(item.getShippingCharge()),
                        nullToEmpty(item.getShippingFeeId()),
                        toStringSafe(item.getCreatedAt()),
                        toStringSafe(item.getUpdatedAt())
                    };
                    csvWriter.writeNext(data);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV出力に失敗しました", e);
        }
        return null;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
    private String toStringSafe(Object obj) {
        return obj == null ? "" : obj.toString();
    }
    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @PostMapping("/upload-serial-images")
    public String uploadSerialImages(@RequestParam("files") MultipartFile[] files, Model model) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) continue;
            String sku = originalFilename.contains(".")
                ? originalFilename.substring(0, originalFilename.lastIndexOf(".")) : originalFilename;

            // SKUで複数商品取得
            List<MercariItem> items = mercariItemRepository.findBySku(sku);
            for (MercariItem item : items) {
                try {
                    String path = "src/main/resources/static/images/serials/" + originalFilename;
                    file.transferTo(new java.io.File(path));
                    item.setImageSerial("/images/serials/" + originalFilename);
                    mercariItemRepository.save(item);
                } catch (IOException e) {
                    e.printStackTrace();
                    // 必要ならエラー通知
                }
            }
        }
        model.addAttribute("message", "アップロード完了しました！");
        return "redirect:/items";
    }

    @GetMapping("/stock")
    public String skuStockList(Model model) {
        List<Object[]> skuStock = mercariItemRepository.getSkuStockSummary();
        Map<String, List<MercariItem>> skuToItems = new HashMap<>();
        for (Object[] row : skuStock) {
            String sku = (String) row[0];
            skuToItems.put(sku, mercariItemRepository.findBySku(sku));
        }
        model.addAttribute("skuStock", skuStock);
        model.addAttribute("skuToItems", skuToItems);
        return "sku_stock_list"; // テンプレート名
    }
    @GetMapping("/stock/edit/{sku}")
    public String editSkuStock(@PathVariable String sku, Model model) {
        List<MercariItem> items = mercariItemRepository.findBySku(sku);
        model.addAttribute("items", items);
        model.addAttribute("sku", sku);
        return "sku_stock_edit";
    }

    @PostMapping("/stock/update")
    public String updateSkuStock(@RequestParam String sku, @RequestParam Integer stock) {
        List<MercariItem> items = mercariItemRepository.findBySku(sku);
        for (MercariItem item : items) {
            item.setStock(stock); // 一括変更
            mercariItemRepository.save(item);
        }
        return "redirect:/stock";
    }
    @PostMapping("/batch-set-mall-mercari")
    @Transactional
    public String batchSetMallMercari(Model model) {
        Mall mercariMall = mallRepository.findByCode("MERCARI");
        if (mercariMall == null) {
            model.addAttribute("error", "メルカリモール情報が存在しません。");
            return "redirect:/items";
        }
        List<MercariItem> items = mercariItemRepository.findAll();
        for (MercariItem item : items) {
            item.setMall(mercariMall);
        }
        mercariItemRepository.saveAll(items);
        mercariItemRepository.flush();        // ←この行を追加！
        model.addAttribute("message", "全件モール=メルカリに設定しました");
        return "redirect:/items";
    }
}