package Actividades.Ingreso;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import Recursos.Interfaz;

public class Ingreso {
    private int viajes = 0;
    private CyclicBarrier colectivo = new CyclicBarrier(26, () -> {System.out.println("INGRESO --- Comienza tour por colectivo folklórico!"); incrementarViajes();});
    private Semaphore molinetes = new Semaphore(6);
    private Interfaz interfaz;

    public Ingreso(Interfaz interfaz) {
        this.interfaz = interfaz;
    }

    public void ingresarPorTour() throws InterruptedException {
        System.out.println("INGRESO --- " + Thread.currentThread().getName() + " entró a colectivo folklórico.");
        try {
            interfaz.aumentarPasajeros();
            colectivo.await();
        } catch (BrokenBarrierException e) {
            // significa que el colectivo arrancó con menos de 25 pasajeros
        } finally {
            interfaz.decrementarPasajeros();
        }
    }

    public void entrarMolinete() throws InterruptedException {
        Random r = new Random();
        molinetes.acquire();
        System.out.println("INGRESO --- " + Thread.currentThread().getName() + " entró al molinete.");
        Thread.sleep(r.nextInt(500,1000));
        molinetes.release();
        System.out.println("INGRESO --- " + Thread.currentThread().getName() + " salió del molinete.");
    }

    public void incrementarViajes() {
        viajes++;
        interfaz.setViajes(viajes);
    }

    public CyclicBarrier getColectivo() {
        return colectivo;
    }
}
