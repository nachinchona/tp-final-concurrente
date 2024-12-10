package Actividades.Snorkel;

import java.util.Random;

import Recursos.EquipoSnorkel;

public class Asistente extends Thread {
    private EquipoSnorkel equipos;
    private Random r = new Random();

    public Asistente(EquipoSnorkel equipos) {
        this.equipos = equipos;
    }

    public void run() {
        while(true) {
            try {
                equipos.obtenerVisitante();
                equipos.tomarEquipo();
                equipos.entregarEquipo();
                Thread.sleep(r.nextInt(200,1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
