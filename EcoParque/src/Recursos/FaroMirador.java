package Recursos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class FaroMirador {
    private BlockingQueue<Visitante> escalera = new ArrayBlockingQueue<>(10);
    private Semaphore tobogan = new Semaphore(0, true);
    private int toboganesEnUso = 0;
    private Semaphore tirarse = new Semaphore(0);
    private int personasEsperando = 0;
    private Semaphore mutex = new Semaphore(1, true);

    public void liberarVisitante() {
        escalera.poll();
    }

}
