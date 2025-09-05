package com.example.mercari.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.mercari.entity.RakutenItem;
import com.example.mercari.repository.RakutenItemRepository;
import com.example.mercari.service.RakutenCsvImportService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/rakuten-items")
public class RakutenItemController {

    private final RakutenItemRepository rakutenItemRepository;
    private final RakutenCsvImportService rakutenCsvImportService;

    // 楽天商品一覧
    @GetMapping
    public String listItems(Model model) {
        List<RakutenItem> items = rakutenItemRepository.findAll();
        model.addAttribute("rakutenItems", items);
        return "rakuten_item_list";
    }

    // 商品登録フォーム
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("rakutenItem", new RakutenItem());
        return "rakuten_item_form";
    }

    // 商品保存
    @PostMapping("/save")
    public String saveItem(@ModelAttribute RakutenItem rakutenItem) {
        rakutenItemRepository.save(rakutenItem);
        return "redirect:/rakuten-items";
    }

    // 商品詳細
    @GetMapping("/{id}")
    public String viewItem(@PathVariable Long id, Model model) {
        RakutenItem item = rakutenItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無効なID: " + id));
        model.addAttribute("rakutenItem", item);
        return "rakuten_item_detail";
    }

    // 編集フォーム
    @GetMapping("/edit/{id}")
    public String editItem(@PathVariable Long id, Model model) {
        RakutenItem item = rakutenItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("無効なID: " + id));
        model.addAttribute("rakutenItem", item);
        return "rakuten_item_form";
    }

    // 削除
    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        rakutenItemRepository.deleteById(id);
        return "redirect:/rakuten-items";
    }

    // CSVアップロードフォーム
    @GetMapping("/upload")
    public String showUploadForm() {
        return "rakuten_item_upload";
    }

    // CSVアップロード処理
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            rakutenCsvImportService.importFromMultipartFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/rakuten-items";
    }
}