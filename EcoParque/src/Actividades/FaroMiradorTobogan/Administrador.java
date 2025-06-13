package Actividades.FaroMiradorTobogan;

import java.util.Random;

import Recursos.EcoParque;
import Recursos.FaroMirador;

public class Administrador extends Thread {
    private EcoParque parque;
    private FaroMirador faro;
    private Random r = new Random();

    public Administrador(EcoParque parque) {
        this.parque = parque;
        this.faro = parque.getFaroMirador();
    }

    public void run() {
        System.out.println("FARO MIRADOR --- Administrador listo.");
        while (parque.sePuedenRealizarActividades()) {
            try {
                faro.asignarTobogan();
                faro.avisarVisitante();
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        faro.cerrar();
    }
}
