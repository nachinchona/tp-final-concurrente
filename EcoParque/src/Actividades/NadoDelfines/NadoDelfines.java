package Actividades.NadoDelfines;

import Recursos.Pileta;

public class NadoDelfines {
    private final Pileta[] piletas = new Pileta[4];
    private int piletasOcupadas = 0;

    private boolean sePuedeNadar = false;


    public NadoDelfines() {
        for (int i = 0; i < piletas.length; i++) {
            piletas[i] = new Pileta();
        }
    }

    public synchronized Pileta buscarPileta() {
        Pileta pileta = null;
        int i = 0;
        boolean encontrado = false;
        while (!encontrado && !sePuedeNadar) {
            if (!piletas[i].estaOcupada()) {
                pileta = piletas[i];
                boolean rta = pileta.ingresar();
                if (rta) {
                    piletasOcupadas++;
                }
                encontrado = true;
            }
            i++;
        }
        return pileta;
    }

    public synchronized void nadar() throws InterruptedException {
        while (!sePuedeNadar) {
            this.wait();
        }
        if (sePuedeNadar) {
            System.out.println(Thread.currentThread().getName() + " está nadando");
            this.wait();
        } else {
            System.out.println("CHAO ME VOY");
        }
    }

    public synchronized void esperarPiletasLlenas() throws InterruptedException {
        this.wait();
        //Espera hasta el horario de la actividad
        //Una vez que llega el horario avisa al resto que pueden nadar
        if (piletasOcupadas >= 3) {
            sePuedeNadar = true;
            System.out.println("ARRANCA LA ACTIVIDAD LOCO");
            this.wait();
            System.out.println("SE TERMINA LA ACTIVIDAD, SALGAN");
        } else {
            System.out.println("NO SE ALCANZO EL CUPO DE PILETAS");
        }
    }
}
