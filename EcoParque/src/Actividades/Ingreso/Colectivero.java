package Actividades.Ingreso;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Recursos.EcoParque;

public class Colectivero extends Thread {
    private EcoParque parque;
    private Ingreso ingreso;
    private CyclicBarrier colectivo;

    public Colectivero(EcoParque parque) {
        this.parque = parque;
        this.ingreso = parque.getIngreso();
        this.colectivo = ingreso.getColectivo();
    }

    public void run() {
        while (parque.estaAbierto()) {
            try {
                // viaje común con 25 pasajeros + colectivero
                colectivo.await(10000, TimeUnit.MILLISECONDS);
                Thread.sleep(3000);
            } catch (BrokenBarrierException | TimeoutException e) {
                // viaje comienza con menos de 25 pasajeros
                // los pasajeros deben hacer catch de la excepción BrokenBarrierException para
                // reflejar el viaje incompleto
                synchronized (colectivo) {
                    if (parque.estaAbierto()) {
                        if (ingreso.hayPasajerosEsperando()) {
                            ingreso.incrementarViajes(); // ya que no lo hace la barrera
                            //System.out.println("INGRESO --- Comienza viaje folklórico con menos de 25 pasajeros.");
                        }
                    } else {
                        //System.out.println("INGRESO --- Viaje no comenzó porque el parque está cerrado.");
                    }
                    colectivo.reset();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (colectivo) {
            if (colectivo.getNumberWaiting() > 0) {
                // ultimo viaje ya que el parque cerró
                ingreso.incrementarViajes();
            }
            colectivo.reset();
        }
    }
}
