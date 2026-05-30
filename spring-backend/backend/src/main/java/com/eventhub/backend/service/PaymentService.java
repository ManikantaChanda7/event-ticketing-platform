package com.eventhub.backend.service;

import com.eventhub.backend.dto.PaymentRequest;
import com.eventhub.backend.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse makePayment(PaymentRequest request);
}