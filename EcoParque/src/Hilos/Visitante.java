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
            int siguienteActividad = r.nextInt(0,5);
            try {
                parque.realizarActividad(siguienteActividad, this);
                //quiereIrse = r.nextInt(1000) > 998;
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        
        if (!parque.sePuedenRealizarActividades()) {
            System.out.println("HASTA PRONTO! --- El visitante " + this.getName() + " se retira del parque.");
        } else {
            if (quiereIrse) {
                System.out.println("HASTA PRONTO! --- " + this.getName() + " quiso irse antes de tiempo de EcoParque.");
            }
        }
        parque.salir();
    }
}
