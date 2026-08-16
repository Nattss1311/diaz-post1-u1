// VipDiscount.java
package com.patrones.u1;

public class VipDiscount implements DiscountStrategy {
    public double apply(double total) { return total * 0.85; }
}