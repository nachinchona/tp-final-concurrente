package Hilos;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;

import Recursos.EcoParque;

public class Visitante extends Thread {
    EcoParque parque;
    Random r = new Random();

    public Visitante(String nombre, EcoParque parque) {
        this.setName(nombre);
        this.parque = parque;
    }

    public void run() {
        Random r = new Random();
        if (parque.estaAbierto()) {
            try {
                int random = r.nextInt(20);
                parque.ingresar(random < 17);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("CERRADO --- EcoParque cerró sus puertas. Vuelva mañana a las 9:00 am!");
        }
        boolean quiereIrse = false; 
        while (parque.sePuedenRealizarActividades() && !quiereIrse) {
            int siguienteActividad = r.nextInt(4);
            try {
                parque.realizarActividad(siguienteActividad, this);
                quiereIrse = r.nextInt(20) > 18;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (quiereIrse) {
            parque.salir();
            System.out.println("HASTA PRONTO! --- " + this.getName() + " quiso irse antes de tiempo de EcoParque.");
        }
    }
}
