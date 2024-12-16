package Actividades.Tienda;

import Recursos.EcoParque;
import Recursos.Tienda;

public class Cajero extends Thread {
    private EcoParque parque;
    private Tienda tienda;

    public Cajero(EcoParque parque) {
        this.parque = parque;
        this.tienda = parque.getTienda();
    }

    public void run() {
        System.out.println("TIENDA --- Cajero listo.");
        while (parque.sePuedenRealizarActividades()) {
            try {
                tienda.atender();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}