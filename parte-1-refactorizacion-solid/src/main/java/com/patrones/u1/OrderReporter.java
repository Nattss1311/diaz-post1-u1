package com.patrones.u1;

import java.util.List;

public class OrderReporter {
    public void print(List orders) {
        System.out.println("=== Reporte de Órdenes ===");
        orders.forEach(o -> System.out.println("  " + o));
    }
}
