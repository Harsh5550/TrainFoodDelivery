package com.harsh.trainfooddelivery.listeners;

import com.harsh.trainfooddelivery.models.Order;

public interface OrderListener {
    void onAccepted(Order order);
    void onRejected(Order order, int position);
}
