package Actividades.CarreraGomones;

import Recursos.EcoParque;
import Recursos.Gomones;

public class Chofer extends Thread {
    private Gomones gomones;

    public Chofer(EcoParque parque) {
        this.gomones = parque.getGomones();
    }

    public void run() {
        System.out.println("GOMONES --- Chofer listo.");
        while (true) {
            gomones.controlTren();
        }
    }
}
