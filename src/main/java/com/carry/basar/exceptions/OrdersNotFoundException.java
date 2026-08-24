package com.carry.basar.exceptions;

public class OrdersNotFoundException extends RuntimeException {

  public OrdersNotFoundException(String message) {
    super(message);
  }

}