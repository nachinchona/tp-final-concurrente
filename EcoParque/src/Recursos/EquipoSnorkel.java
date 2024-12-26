package Recursos;

import java.util.LinkedList;
import java.util.Queue;

import Hilos.Visitante;

public class EquipoSnorkel {
    private EcoParque parque;
    private int equiposDisponibles = 5;
    private Queue<Visitante> fila = new LinkedList<>();
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    public EquipoSnorkel(EcoParque parque) {
        this.parque = parque;
    }

    // visitante hace fila, el visitante notificado sale de la fila solo si el es el frente de la cola
    public synchronized boolean hacerFila(Visitante visitante) throws InterruptedException {
        fila.add(visitante);
        System.out.println(ANSI_RED + "SNORKEL --- " + Thread.currentThread().getName() + " está haciendo fila para hacer snorkel." + ANSI_RESET);
        this.notifyAll();
        while ((!fila.peek().equals(visitante) || equiposDisponibles == 0) && parque.sePuedenRealizarActividades()) {
            this.wait();
        }
        fila.remove(visitante);
        if (parque.sePuedenRealizarActividades()) {
            equiposDisponibles--;
            System.out.println("SNORKEL --- " + Thread.currentThread().getName() + " dejó de hacer fila para hacer snorkel.");
        } else {
            System.out.println("SNORKEL --- " + Thread.currentThread().getName() + " dejó de hacer fila para hacer snorkel porque se cerró el parque.");
        }
        return parque.sePuedenRealizarActividades();
    }

    public synchronized void hacerSnorkel() throws InterruptedException {
        System.out.println("SNORKEL --- " + Thread.currentThread().getName() + " recibe equipo y empieza a hacer snorkel.");
    }

    // asistente atiende al cliente
    public synchronized void atenderCliente() throws InterruptedException {
        while((fila.isEmpty() || equiposDisponibles == 0) && parque.sePuedenRealizarActividades()) {
            this.wait();
        }
        this.notifyAll();
    }

    // visitante termina de usar equipo, lo devuelve y notifica a todos (para incluir a los asistentes)
    public synchronized void dejarEquipo() {
        equiposDisponibles++;
        System.out.println(ANSI_GREEN + "SNORKEL --- " + Thread.currentThread().getName() + " dejó de hacer snorkel." + ANSI_RESET);
        this.notifyAll();
    }

    public synchronized void cerrar() {
        fila.clear();
        this.notifyAll();
    }
}
