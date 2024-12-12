package Hilos;

import java.util.Random;

import Recursos.EcoParque;

public class Visitante extends Thread {
    EcoParque parque;
    Random r = new Random();

    public Visitante(String nombre, EcoParque parque) {
        this.setName(nombre);
        this.parque = parque;
    }

    public void run() {
        while (true) {
            try {
                parque.realizarActividad(this);
                Thread.sleep(r.nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
