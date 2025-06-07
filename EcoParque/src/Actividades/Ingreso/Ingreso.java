package Actividades.Ingreso;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import Recursos.EcoParque;
import Recursos.Interfaz;

public class Ingreso {
    private EcoParque parque;
    private int viajes = 0;
    private boolean hayPasajerosEsperando = false;
    private int pasajerosEsperando = 0;
    private CyclicBarrier colectivo = new CyclicBarrier(26, () -> {
        //System.out.println("INGRESO --- Comienza tour por colectivo folklórico!");
        incrementarViajes();
    });
    private Semaphore molinetes = new Semaphore(6);
    private Interfaz interfaz;

    public Ingreso(Interfaz interfaz, EcoParque parque) {
        this.interfaz = interfaz;
        this.parque = parque;
    }

    public void aumentarPasajeros() {
        pasajerosEsperando++;
        interfaz.aumentarPasajeros();
    }

    public void decrementarPasajeros() {
        pasajerosEsperando--;
        interfaz.decrementarPasajeros();
    }

    public boolean hayPasajerosEsperando() {
        return pasajerosEsperando != 0;
    }

    public void ingresarPorTour() throws InterruptedException {
        //System.out.println("INGRESO --- " + Thread.currentThread().getName() + " entró a colectivo folklórico.");
        try {
            aumentarPasajeros();
            colectivo.await();
        } catch (BrokenBarrierException e) {
            // significa que el colectivo arrancó con menos de 25 pasajeros
        } finally {
            decrementarPasajeros();
        }
    }

    public void entrarMolinete() throws InterruptedException {
        if (parque.estaAbierto()) {
            Random r = new Random();
            molinetes.acquire();
            //System.out.println("INGRESO --- " + Thread.currentThread().getName() + " entró al molinete.");
            Thread.sleep(r.nextInt(500, 1000));
            molinetes.release();
            //System.out.println("INGRESO --- " + Thread.currentThread().getName() + " salió del molinete.");
        }
    }

    public void incrementarViajes() {
        viajes++;
        interfaz.setViajes(viajes);
    }

    public CyclicBarrier getColectivo() {
        return colectivo;
    }
}
