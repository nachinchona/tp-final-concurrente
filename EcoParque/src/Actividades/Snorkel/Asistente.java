package Actividades.Snorkel;

import java.util.Random;

import Recursos.EcoParque;
import Recursos.EquipoSnorkel;

public class Asistente extends Thread {
    private EcoParque parque;
    private EquipoSnorkel equipos;
    private Random r = new Random();

    public Asistente(EcoParque parque) {
        this.parque = parque;
        this.equipos = parque.getEquipoSnorkel();
    }

    public void run() {
        System.out.println("SNORKEL --- Asistente listo.");
        while(parque.sePuedenRealizarActividades()) {
            try {
                equipos.atenderCliente();
                Thread.sleep(r.nextInt(200,1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!parque.sePuedenRealizarActividades()) {
            equipos.cerrar();
        }
    }
}
