package Actividades.FaroMiradorTobogan;

import java.util.Random;

import Recursos.FaroMirador;

public class Administrador extends Thread{
    private FaroMirador faro;
    private Random r = new Random();

    public Administrador(FaroMirador faro) {
        this.faro = faro;
    }

    public void run() {
        while (true) {
            boolean toboganAsignado = r.nextBoolean();
            try {
                faro.asignarTobogan(toboganAsignado);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
