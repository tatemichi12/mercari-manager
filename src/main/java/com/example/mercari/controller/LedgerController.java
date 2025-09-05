package com.example.mercari.controller;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.mercari.entity.AuctionLot;
import com.example.mercari.entity.LedgerEntry;
import com.example.mercari.repository.AuctionLotRepository;
import com.example.mercari.repository.LedgerEntryRepository;

@Controller
@RequestMapping("/ledger")
public class LedgerController {
    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;
    @Autowired
    private AuctionLotRepository auctionLotRepository;

    // 一覧＋検索
    @GetMapping
    public String list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) String itemName,
        @RequestParam(required = false) String sku,
        @RequestParam(required = false) String partner,
        @RequestParam(required = false) String mall,
        Model model
    ) {
        List<LedgerEntry> ledgerEntries = ledgerEntryRepository.findAll().stream()
            .filter(entry -> fromDate == null || !entry.getDate().isBefore(fromDate))
            .filter(entry -> toDate == null || !entry.getDate().isAfter(toDate))
            .filter(entry -> itemName == null || entry.getItemName().contains(itemName))
            .filter(entry -> sku == null || entry.getSku().contains(sku))
            .filter(entry -> partner == null || entry.getLedgerPartner().contains(partner))
            .filter(entry -> mall == null || entry.getMall().contains(mall))
            .toList();
        model.addAttribute("ledgerEntries", ledgerEntries);
        return "ledger_list";
    }

    // 編集フォーム表示
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, @RequestParam(required = false) String returnUrl, Model model) {
        LedgerEntry entry = ledgerEntryRepository.findById(id).orElse(null);
        model.addAttribute("ledgerEntry", entry);

        List<AuctionLot> auctionLots = auctionLotRepository.findAll();
        model.addAttribute("auctionLots", auctionLots);

        Long selectedAuctionId = (entry != null && entry.getAuctionLot() != null) ? entry.getAuctionLot().getId() : null;
        model.addAttribute("selectedAuctionId", selectedAuctionId);

        // 追加：returnUrlのModelセット
        model.addAttribute("returnUrl", returnUrl != null ? returnUrl : "/ledger");
        return "ledger_edit";
    }

    // 編集内容保存
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute LedgerEntry form, @RequestParam(required = false) String returnUrl) {
        form.setId(id);
        ledgerEntryRepository.save(form);
        return "redirect:" + (returnUrl != null ? returnUrl : "/ledger");
    }

    // 削除
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @RequestParam(required = false) String returnUrl) {
        ledgerEntryRepository.deleteById(id);
        return "redirect:" + (returnUrl != null ? returnUrl : "/ledger");
    }
    
    // 新規登録画面
 // 新規登録画面
    @GetMapping("/new")
    public String newForm(
        @RequestParam(value = "auctionId", required = false) Long auctionId,
        Model model
    ) {
        LedgerEntry entry = new LedgerEntry();
        if (auctionId != null) {
            entry.setAuctionLot(auctionLotRepository.findById(auctionId).orElse(null));
            model.addAttribute("selectedAuctionId", auctionId);
        }
        model.addAttribute("ledgerEntry", entry);
        model.addAttribute("auctionLots", auctionLotRepository.findAll());
        // model.addAttribute("auctionLot", new AuctionLot()); // ←不要
        return "ledger_new";
    }

    // 登録処理
    @PostMapping("/new")
    public String createLedgerEntry(@ModelAttribute LedgerEntry ledgerEntry, Model model) {
        AuctionLot auctionLot = null;

        // 既存オークション選択
        if (ledgerEntry.getAuctionLot() != null && ledgerEntry.getAuctionLot().getId() != null) {
            auctionLot = auctionLotRepository.findById(ledgerEntry.getAuctionLot().getId()).orElse(null);
        }
        // 日付＆パートナー一致のAuctionLotまとめ処理
        else if (ledgerEntry.getAuctionPartner() != null && !ledgerEntry.getAuctionPartner().isEmpty()) {
            List<AuctionLot> lots = auctionLotRepository.findByDateAndAuctionPartner(ledgerEntry.getDate(), ledgerEntry.getAuctionPartner());
            if (!lots.isEmpty()) {
                auctionLot = lots.get(0); // 既存オークションを使う
            } else {
                AuctionLot lot = new AuctionLot();
                lot.setDate(ledgerEntry.getDate());
                lot.setAuctionPartner(ledgerEntry.getAuctionPartner());
                // 他の項目も必要ならセット
                auctionLot = auctionLotRepository.save(lot); // 新規作成
            }
        }

        // auctionLotがnullなら保存しない
        if (auctionLot == null) {
            model.addAttribute("error", "オークションを選択するか新規入力してください");
            model.addAttribute("ledgerEntry", ledgerEntry);
            model.addAttribute("auctionLots", auctionLotRepository.findAll());
            return "ledger_new";
        }

        ledgerEntry.setAuctionLot(auctionLot);
        ledgerEntryRepository.save(ledgerEntry);

        return "redirect:/ledger";
    }
    // PDF出力
    @GetMapping("/pdf")
    public void exportPdf(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) String itemName,
        @RequestParam(required = false) String sku,
        @RequestParam(required = false) String partner,
        @RequestParam(required = false) String mall,
        HttpServletResponse response
    ) throws Exception {
        List<LedgerEntry> ledgerEntries = ledgerEntryRepository.findAll().stream()
            .filter(entry -> fromDate == null || !entry.getDate().isBefore(fromDate))
            .filter(entry -> toDate == null || !entry.getDate().isAfter(toDate))
            .filter(entry -> itemName == null || entry.getItemName().contains(itemName))
            .filter(entry -> sku == null || entry.getSku().contains(sku))
            .filter(entry -> partner == null || entry.getLedgerPartner().contains(partner))
            .filter(entry -> mall == null || entry.getMall().contains(mall))
            .toList();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=ledger.pdf");

        try (OutputStream out = response.getOutputStream()) {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
            document.open();

            document.add(new com.lowagie.text.Paragraph("古物台帳一覧"));

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(11);
            table.addCell("日付");table.addCell("区別");table.addCell("SKU");table.addCell("特徴");table.addCell("数量");
            table.addCell("代価");table.addCell("品目");table.addCell("取引相手");table.addCell("住所");table.addCell("氏名");table.addCell("モール");

            for (LedgerEntry entry : ledgerEntries) {
                table.addCell(String.valueOf(entry.getDate()));
                table.addCell(entry.getType());
                table.addCell(entry.getSku());
                table.addCell(entry.getFeature());
                table.addCell(String.valueOf(entry.getQuantity()));
                table.addCell(String.valueOf(entry.getPrice()));
                table.addCell(entry.getItemName());
                table.addCell(entry.getLedgerPartner());
                table.addCell(entry.getAddress());
                table.addCell(entry.getPartnerName());
                table.addCell(entry.getMall());
            }
            document.add(table);
            document.close();
        }
    }
}