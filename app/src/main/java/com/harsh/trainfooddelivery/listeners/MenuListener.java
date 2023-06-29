package com.harsh.trainfooddelivery.listeners;

import com.harsh.trainfooddelivery.models.Menu;

public interface MenuListener {
    void onEditClicked(Menu menu);
    void onDeleteClicked(Menu menu, int position);
    void onCount(int count, int position);
}
