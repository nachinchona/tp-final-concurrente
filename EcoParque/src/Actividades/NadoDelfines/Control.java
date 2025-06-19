package Actividades.NadoDelfines;

import Recursos.EcoParque;
import Recursos.NadoDelfines;

public class Control extends Thread {
    // controla que se llenen al menos 3 piletas

    private NadoDelfines nadoDelfines;
    private EcoParque parque;

    public Control(EcoParque park) {
        this.parque = park;
        this.nadoDelfines = parque.getNadoDelfines();
    }

    public void run() {
        while(true) {
            try {
                nadoDelfines.esperarPiletasLlenas();
                if (nadoDelfines.getSePuedeNadar()) {
                    nadoDelfines.esperarPiletasVacias();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
