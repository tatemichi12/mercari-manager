package com.example.mercari.form;

import java.util.List;

import com.example.mercari.entity.MercariItem;

public class ItemListForm {
    private List<MercariItem> items;

    public List<MercariItem> getItems() {
        return items;
    }
    public void setItems(List<MercariItem> items) {
        this.items = items;
    }
}