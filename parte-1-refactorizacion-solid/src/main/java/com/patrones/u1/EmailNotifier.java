package com.patrones.u1;

public class EmailNotifier {
    public void sendConfirmation(String email, String orderId) {
        System.out.println("[EMAIL] Enviando a " + email
            + " confirmación de orden " + orderId);
    }
}
