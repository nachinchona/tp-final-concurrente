package Actividades.CarreraGomones;

import Hilos.Visitante;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GomonDoble extends Gomon {
    private final Lock lock = new ReentrantLock();
    private final Condition parejaLista = lock.newCondition();
    private final Condition esperar = lock.newCondition();
    private Visitante segundoVisitante;
    boolean primerTripulanteEsperando = false;

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

    public void esperar() throws InterruptedException {
        lock.lock();
        esperar.await();
        lock.lock();
    }
    public void avisar()  {
        lock.lock();
        esperar.signal();
        lock.unlock();
    }

    public void subirAlGomon(Visitante visitante) throws InterruptedException {
        lock.lock();
        try {
            if (!primerTripulanteEsperando) {
                // Primer tripulante espera a su pareja
                primerTripulanteEsperando = true;
                this.añadirVisitante(visitante);
                parejaLista.await();  // se bloquea hasta que llegue otro
            } else {
                // Segundo tripulante ha llegado
                this.añadirCompañero(visitante);
                parejaLista.signal(); // despierta al primero
                primerTripulanteEsperando = false;
            }
        } finally {
            lock.unlock();
        }
    }
}
