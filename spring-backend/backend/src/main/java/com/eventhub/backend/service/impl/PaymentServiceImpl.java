package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.PaymentRequest;
import com.eventhub.backend.dto.PaymentResponse;
import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.entity.Payment;
import com.eventhub.backend.enums.BookingStatus;
import com.eventhub.backend.enums.PaymentStatus;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.repository.PaymentRepository;
import com.eventhub.backend.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public PaymentResponse makePayment(PaymentRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentGateway("SIMULATED");
        payment.setTransactionId(
                UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(savedPayment.getId());
        response.setBookingId(booking.getId());
        response.setAmount(savedPayment.getAmount());
        response.setStatus(savedPayment.getStatus().name());
        response.setTransactionId(savedPayment.getTransactionId());
        response.setMessage("Payment Successful");

        return response;
    }
}