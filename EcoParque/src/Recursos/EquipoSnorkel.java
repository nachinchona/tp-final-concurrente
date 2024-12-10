package Recursos;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class EquipoSnorkel {
    // cada permiso comprende el equipo completo (snorkel, salvavidas y patas de rana)
    private Semaphore equiposDisponibles = new Semaphore(5, true);
    private Semaphore esperaEquipos = new Semaphore(0);
    private BlockingQueue<Visitante> fila = new LinkedBlockingQueue<>();
    private Semaphore hayVisitantes = new Semaphore(0);

    public void obtenerVisitante() throws InterruptedException {
        if (!fila.isEmpty()) {
            fila.poll();
        } else {
            hayVisitantes.acquire();
        }
    }

    // visitante hace fila y avisa a asistente
    public void hacerFila(Visitante visitante) {
        fila.add(visitante);
        hayVisitantes.release();
    }

    // asistente avisa que hay equipo libre y se lo entrega
    public void entregarEquipo() {
        esperaEquipos.release();
    }

    // visitante espera equipo
    public void recibirEquipo() throws InterruptedException {
        esperaEquipos.acquire();
    }

    // asistente toma uno de los equipos disponibles para entregarlo luego
    public void tomarEquipo() throws InterruptedException {
        equiposDisponibles.acquire();
    }

    // visitante termina de usar equipo, lo devuelve
    public void dejarEquipo() {
        equiposDisponibles.release();
    }
}
