// NoDiscount.java
package com.patrones.u1;

public class NoDiscount implements DiscountStrategy {
    public double apply(double total) { return total; }
}