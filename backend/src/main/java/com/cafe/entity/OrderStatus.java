package com.cafe.entity;

public enum OrderStatus {
    PLACED,     // just created by user
    ACCEPTED,   // worker accepted it
    PREPARING,  // worker is making it
    READY,      // ready for pickup/serving
    COMPLETED,  // handed to customer
    CANCELLED   // cancelled by user or admin
}
