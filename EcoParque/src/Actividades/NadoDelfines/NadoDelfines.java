package Actividades.NadoDelfines;

import Recursos.Pileta;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NadoDelfines {
    private final Pileta[] piletas = new Pileta[4];
    private final Lock lockPileta = new ReentrantLock(); // Lock para manejar pasajeros
    private final Condition esperarAviso = lockPileta.newCondition();
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
    public synchronized void salirPileta(Pileta pileta) throws InterruptedException {
        if (!pileta.salir()) {
            piletasOcupadas--;
            if (piletasOcupadas == 0 && sePuedeNadar) {
                this.notify();
            }
        }
    }
    public synchronized void nadar() throws InterruptedException {
        esperarAviso.await();
        if (sePuedeNadar) {
            //Solo si se cumple el cupo
            System.out.println(Thread.currentThread().getName() + " está nadando");
            esperarAviso.await();
        } else {
            //Si no se cumple el cupo se van
            System.out.println("CHAO ME VOY");
        }
    }

    public synchronized void esperarPiletasLlenas() throws InterruptedException {
        this.wait();
        //Espera hasta el horario de la actividad
        //Una vez que llega el horario avisa al resto que pueden nadar
        if (piletasOcupadas >= 3) {
            sePuedeNadar = true;
            esperarAviso.signalAll();
            System.out.println("ARRANCA LA ACTIVIDAD LOCO");
            //Espera a que llegue el fin de la actividad
            this.wait();
            System.out.println("SE TERMINA LA ACTIVIDAD, SALGAN");
        } else {
            System.out.println("NO SE ALCANZO EL CUPO DE PILETAS, VAYANSE");
        }
        esperarAviso.signalAll();

    }
}
