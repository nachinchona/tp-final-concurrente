package Actividades.CarreraGomones;

import Hilos.Visitante;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GomonDoble extends Gomon {
    private final Lock lock = new ReentrantLock();
    private final Condition parejaLista = lock.newCondition();
    private final Condition esperar = lock.newCondition();
    private Visitante segundoVisitante;
    boolean soyElPrimero = true;

    public GomonDoble(int numeroGomon) {
        super(numeroGomon);
    }

    public void añadirVisitante(Visitante visitante) {
        super.añadirVisitante(visitante);
    }

    public boolean esPrimerVisitante(Visitante visitante) {
        return super.esPrimerVisitante(visitante);
    }

    public void añadirCompañero(Visitante visitante) {
        this.segundoVisitante = visitante;
    }

    public Visitante getSegundoVisitante() {
        return segundoVisitante;
    }

    public String toString() {
        return super.toString() + "-" + (segundoVisitante == null ? "none":segundoVisitante.getName());
    }

    public void reiniciar() {
        super.reiniciar();
        segundoVisitante = null;
    }

    public void esperarCompañero(boolean parqueAbierto) throws InterruptedException {
        lock.lock();
        boolean bandera = false;
        try {
            while (parqueAbierto && !bandera) {
                // Espera con tiempo límite para verificar periódicamente
                if (esperar.await(10, TimeUnit.SECONDS)) {
                    bandera = true;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void interrumpirEspera() {
        lock.lock();
        try {
            esperar.signalAll(); // Despierta a todos los que están esperando
        } finally {
            lock.unlock();
        }
    }

    public void avisar() {
        lock.lock();
        try {
            esperar.signal();
        } finally {
            lock.unlock();
        }
    }

    public void subirAlGomon(Visitante visitante) throws InterruptedException {
        lock.lock();
        try {
            if (soyElPrimero) {
                // Primer tripulante espera a su pareja
                soyElPrimero = false;
                this.añadirVisitante(visitante);
            } else {
                // Segundo tripulante ha llegado
                this.añadirCompañero(visitante);
                soyElPrimero = true;
                esperar.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}
