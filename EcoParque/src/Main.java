import java.util.Random;

import Hilos.Visitante;
import Recursos.EcoParque;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        EcoParque parque = new EcoParque();
        parque.abrir();

        Random r = new Random();

        Visitante[] visitantes = new Visitante[50];
        for (int i = 0; i < visitantes.length; i++) {
            visitantes[i] = new Visitante("Visitante " + Integer.toString(i), parque);
            Thread.sleep(r.nextInt(400));
            visitantes[i].start();
        }
    }
}
