package com.patrones.u1;

import java.util.List;
import java.util.ArrayList;

// CLASE GOD OBJECT — no modificar en este paso
public class OrderProcessor {
    private List<String> orders = new ArrayList<>();
    private double taxRate = 0.19;

    // Responsabilidad 1: lógica de negocio
    public double calculateTotal(List<Double> prices) {
        double subtotal = 0;
        for (double p : prices) subtotal += p;
        return subtotal + (subtotal * taxRate);
    }

    // Responsabilidad 2: descuentos (segundo algoritmo en la misma clase)
    public double applyDiscount(double total, String customerType) {
        if (customerType.equals("VIP")) return total * 0.85;
        if (customerType.equals("REGULAR")) return total * 0.95;
        return total;
    }

    // Responsabilidad 3: persistencia
    public void saveOrder(String orderId, double total) {
        orders.add(orderId + ":" + total);
        System.out.println("[DB] Orden guardada: " + orderId);
    }

    // Responsabilidad 4: notificación
    public void sendEmail(String email, String orderId) {
        System.out.println("[EMAIL] Enviando a " + email
            + " confirmación de orden " + orderId);
    }

    // Responsabilidad 5: reporte / presentación
    public void printReport() {
        System.out.println("=== Reporte de Órdenes ===");
        for (String o : orders) System.out.println("  " + o);
    }
}
