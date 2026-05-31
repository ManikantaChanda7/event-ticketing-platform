package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.PaymentRequest;
import com.eventhub.backend.dto.PaymentResponse;
import com.eventhub.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public ApiResponse<PaymentResponse> makePayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.makePayment(request);
        return new ApiResponse<>(true, "Payment processed successfully", response);
    }
}