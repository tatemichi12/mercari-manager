package com.example.mercari.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.mercari.entity.AuctionLot;
import com.example.mercari.entity.LedgerEntry;
import com.example.mercari.repository.AuctionLotRepository;
import com.example.mercari.repository.LedgerEntryRepository;

@Controller
@RequestMapping("/auction")
public class AuctionLotController {
    @Autowired
    private AuctionLotRepository auctionLotRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository; // ← これが必要

    @GetMapping("/{id}")
    public String getAuction(@PathVariable Long id, Model model) {
        var auctionLot = auctionLotRepository.findById(id).orElse(null);
        model.addAttribute("auctionLot", auctionLot);
        return "auction_detail"; // src/main/resources/templates/auction/detail.html
    }
    
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("auctionLot", new AuctionLot());
        return "auction_new";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute AuctionLot auctionLot) {
        auctionLotRepository.save(auctionLot);
        return "redirect:/auction/list";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("auctionLots", auctionLotRepository.findAll());
        return "auction_list";
    }

    @GetMapping("/{auctionId}/ledger")
    public String showLedgerEntries(@PathVariable Long auctionId, Model model) {
        AuctionLot auctionLot = auctionLotRepository.findById(auctionId).orElse(null);
        if (auctionLot == null) {
            return "redirect:/auction/list";
        }
        List<LedgerEntry> ledgerEntries = ledgerEntryRepository.findByAuctionLotId(auctionId);
        model.addAttribute("auctionLot", auctionLot);
        model.addAttribute("ledgerEntries", ledgerEntries);
        return "auction_ledger_list";
    }
    // --- 編集画面表示 ---
    @GetMapping("/edit/{id}")
    public String editAuction(@PathVariable Long id, Model model) {
        AuctionLot auctionLot = auctionLotRepository.findById(id).orElse(null);
        if (auctionLot == null) return "redirect:/auction/list";
        model.addAttribute("auctionLot", auctionLot);
        return "auction_edit"; // src/main/resources/templates/auction_edit.html
    }

    // --- 編集保存（更新） ---
    @PostMapping("/edit/{id}")
    public String updateAuction(@PathVariable Long id, @ModelAttribute AuctionLot auctionLot) {
        auctionLot.setId(id); // 念のためIDをセット
        auctionLotRepository.save(auctionLot);
        return "redirect:/auction/list";
    }

    // --- 削除 ---
    @PostMapping("/delete/{id}")
    public String deleteAuction(@PathVariable Long id) {
        auctionLotRepository.deleteById(id);
        return "redirect:/auction/list";
    }
}