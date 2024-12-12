package Recursos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class FaroMirador {
    private BlockingQueue<Visitante> escalera = new ArrayBlockingQueue<>(10);
    private Semaphore tobogan1 = new Semaphore(1, true);
    private Semaphore tobogan2 = new Semaphore(1, true);
    private Semaphore administrador = new Semaphore(0);
    private Semaphore avisarVisitante = new Semaphore(0);
    private boolean asignado; // tobogan asignado por el administrador

    public void avisarAdministrador() {
        administrador.release();
    }
    
    public boolean esperarAviso() throws InterruptedException {
        avisarVisitante.acquire();
        return asignado;
    }

    public void avisarVisitante(boolean valor) {
        avisarVisitante.release();
        asignado = valor;
    }

    public void asignarTobogan(boolean valor) throws InterruptedException {
        administrador.acquire();
    }

    public void usarTobogan(boolean valor) throws InterruptedException {
        if (valor) {
            tobogan1.acquire();
        } else {
            tobogan2.acquire();
        }
        System.out.println(Thread.currentThread().getName() + " se subió al tobogan " + (valor ? "1" : "2"));
    }

    public void liberarTobogan(boolean valor) {
        if (valor) {
            tobogan1.release();
        } else {
            tobogan2.release();
        }
        System.out.println(Thread.currentThread().getName() + " se bajó del tobogan " + (valor ? "1" : "2"));
    }

    public void ocuparEscalon(Visitante visitante) {
        try {
            escalera.add(visitante);
            System.out.println(Thread.currentThread().getName() + " entró a la escalera.");
        } catch (IllegalStateException e) {
            System.out.println("FARO MIRADOR --- Escalera llena");
        }
    }

    public void desocuparEscalon(Visitante visitante) {
        escalera.remove(visitante);
        System.out.println(Thread.currentThread().getName() + " dejó la escalera.");
    }
}
