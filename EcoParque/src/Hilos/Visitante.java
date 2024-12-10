package Hilos;

import java.util.Random;

import Recursos.EcoParque;

public class Visitante extends Thread {
    EcoParque parque;
    Random r = new Random();

    public Visitante(EcoParque parque) {
        this.parque = parque;
    }

    public void run() {
        while (true) {
            int siguienteActividad = r.nextInt(0);
            switch (siguienteActividad) {
                case 0:
                
                    break;
                case 1:

                    break;
                case 2:

                    break;
                case 3:

                    break;
                case 4:

                    break;
                default:
                    break;
            }
        }
    }
}
