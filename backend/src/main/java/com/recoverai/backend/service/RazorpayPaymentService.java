package com.recoverai.backend.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayPaymentService {

    private final RazorpayClient razorpayClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayPaymentService(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret
    ) throws RazorpayException {

        this.keyId = keyId;
        this.keySecret = keySecret;

        this.razorpayClient = new RazorpayClient(
                keyId,
                keySecret
        );
    }

    // --------------------------------------------------
    // Get payment status from Razorpay
    // --------------------------------------------------

    public String getPaymentStatus(String paymentId)
            throws RazorpayException {

        Payment payment =
                razorpayClient.payments.fetch(paymentId);

        return payment.get("status");
    }

    // --------------------------------------------------
    // Verify Razorpay Checkout signature
    // --------------------------------------------------

    public boolean verifySignature(
            String orderId,
            String paymentId,
            String signature
    ) {

        try {

            JSONObject attributes = new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    orderId
            );

            attributes.put(
                    "razorpay_payment_id",
                    paymentId
            );

            attributes.put(
                    "razorpay_signature",
                    signature
            );

            Utils.verifyPaymentSignature(
                    attributes,
                    keySecret
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // --------------------------------------------------
    // Verify payment amount and currency
    // --------------------------------------------------

    public boolean verifyPayment(
            String paymentId,
            double expectedAmount,
            String expectedCurrency
    ) throws RazorpayException {

        Payment payment =
                razorpayClient.payments.fetch(paymentId);

        Object amountObject =
                payment.get("amount");

        Object currencyObject =
                payment.get("currency");

        Object statusObject =
                payment.get("status");

        if (amountObject == null
                || currencyObject == null
                || statusObject == null) {

            return false;
        }

        int razorpayAmount =
                Integer.parseInt(
                        amountObject.toString()
                );

        String razorpayCurrency =
                currencyObject.toString();

        String razorpayStatus =
                statusObject.toString();

        int expectedAmountInPaise =
                (int) Math.round(
                        expectedAmount * 100
                );

        // Amount must match
        if (razorpayAmount != expectedAmountInPaise) {
            return false;
        }

        // Currency must match
        if (!razorpayCurrency.equalsIgnoreCase(
                expectedCurrency
        )) {
            return false;
        }

        // Payment must be failed
return "failed".equalsIgnoreCase(razorpayStatus);
    }

    // --------------------------------------------------
    // Create Razorpay Order
    // --------------------------------------------------

    public Order createOrder(
            double amount,
            String currency,
            String receipt
    ) throws RazorpayException {

        int amountInPaise =
                (int) Math.round(amount * 100);

        JSONObject orderRequest =
                new JSONObject();

        orderRequest.put(
                "amount",
                amountInPaise
        );

        orderRequest.put(
                "currency",
                currency
        );

        orderRequest.put(
                "receipt",
                receipt
        );

        return razorpayClient.orders.create(
                orderRequest
        );
    }

    // --------------------------------------------------
    // Get Razorpay Key ID
    // --------------------------------------------------

    public String getKeyId() {
        return keyId;
    }
}