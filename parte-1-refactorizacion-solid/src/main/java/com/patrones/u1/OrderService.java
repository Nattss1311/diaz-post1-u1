package com.patrones.u1;

import java.util.List;

public class OrderService {
    private final TaxCalculator taxCalculator;
    private final OrderRepository repository;
    private final EmailNotifier notifier;
    private final DiscountStrategy discountStrategy;

    // Inyección por constructor — DIP aplicado
    public OrderService(TaxCalculator taxCalculator,
                        OrderRepository repository,
                        EmailNotifier notifier,
                        DiscountStrategy discountStrategy) {
        this.taxCalculator    = taxCalculator;
        this.repository       = repository;
        this.notifier         = notifier;
        this.discountStrategy = discountStrategy;
    }

    public void processOrder(String orderId, String email, List<Double> prices) {
        double total      = taxCalculator.calculateTotal(prices);
        double discounted = discountStrategy.apply(total);
        repository.save(orderId, discounted);
        notifier.sendConfirmation(email, orderId);
    }
}