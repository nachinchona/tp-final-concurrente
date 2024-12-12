package Recursos;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class EquipoSnorkel {
    // cada permiso comprende el equipo completo (snorkel, salvavidas y patas de rana)
    private Semaphore equiposDisponibles = new Semaphore(5, true);
    private Queue<Visitante> fila = new LinkedList<>();
    private Semaphore esperaEquipos = new Semaphore(0);

    public synchronized void salirFila(Visitante visitante) throws InterruptedException {
        while (!fila.peek().equals(visitante)) {
            this.wait();
        }
        fila.remove(visitante);
        System.out.println(Thread.currentThread().getName() + " dejó de hacer fila para hacer snorkel.");
        this.notify();
    }

    // visitante hace fila y avisa a asistente
    public synchronized void hacerFila(Visitante visitante) throws InterruptedException {
        fila.add(visitante);
        System.out.println(Thread.currentThread().getName() + " está haciendo fila para hacer snorkel.");
        this.wait();
    }

    // asistente avisa que hay equipo libre y se lo entrega
    public void entregarEquipo() {
        esperaEquipos.release();
    }

    // visitante espera equipo
    public void recibirEquipo() throws InterruptedException {
        esperaEquipos.acquire();
        System.out.println(Thread.currentThread().getName() + " recibe equipo para hacer snorkel.");
    }

    // asistente toma uno de los equipos disponibles para entregarlo luego
    public void tomarEquipo() throws InterruptedException {
        equiposDisponibles.acquire();
    }

    // visitante termina de usar equipo, lo devuelve
    public void dejarEquipo() {
        equiposDisponibles.release();
        System.out.println(Thread.currentThread().getName() + " dejó de hacer snorkel.");
    }
}
