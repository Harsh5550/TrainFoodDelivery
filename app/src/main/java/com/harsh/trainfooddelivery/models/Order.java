package com.harsh.trainfooddelivery.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    public String id, passengerId, restaurantId, deliveryId, orderStatus, timestamp, orderOtp, amount, restaurantOtp;
    public List<String> foodItem;
}
