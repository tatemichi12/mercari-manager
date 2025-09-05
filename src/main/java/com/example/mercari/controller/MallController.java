package com.example.mercari.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.mercari.entity.Mall;
import com.example.mercari.repository.MallRepository;

@Controller
@RequestMapping("/malls")
public class MallController {

    @Autowired
    private MallRepository mallRepository;

    @GetMapping("/new")
    public String showMallForm(Model model) {
        model.addAttribute("mall", new Mall());
        return "mall_form";
    }

    @PostMapping("/new")
    public String registerMall(@ModelAttribute Mall mall) {
        mallRepository.save(mall);
        return "redirect:/malls/list";
    }

    @GetMapping("/list")
    public String listMalls(Model model) {
        model.addAttribute("mallList", mallRepository.findAll());
        return "mall_list";
    }
    @PostMapping("/delete/{id}")
    public String deleteMall(@PathVariable Long id) {
        mallRepository.deleteById(id);
        return "redirect:/malls/list";
    }
}