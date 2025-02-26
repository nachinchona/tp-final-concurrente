package Recursos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class FaroMirador {
    private BlockingQueue<Visitante> escalera = new ArrayBlockingQueue<>(10);
    private EcoParque parque;
    private boolean abierto = true;
    private Semaphore tobogan1 = new Semaphore(1, true);
    private Semaphore tobogan2 = new Semaphore(1, true);
    private Semaphore administrador = new Semaphore(0);
    private Semaphore avisarVisitante = new Semaphore(0);
    private boolean asignado; // tobogan asignado por el administrador
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    public FaroMirador(EcoParque parque) {
        this.parque = parque;
    }

    public void avisarAdministrador() {
        administrador.release();
    }

    public boolean esperarAviso() throws InterruptedException {
        avisarVisitante.acquire();
        return asignado;
    }

    public void avisarVisitante() {
        avisarVisitante.release();
    }

    public void asignarTobogan(boolean valor) throws InterruptedException {
        administrador.acquire();
        asignado = valor;
    }

    public boolean usarTobogan(boolean valor) throws InterruptedException {
        if (valor) {
            tobogan1.acquire();
        } else {
            tobogan2.acquire();
        }
        if (abierto) {
            System.out.println(
                "FARO MIRADOR --- " + Thread.currentThread().getName() + " se subió al tobogán " + (valor ? "1" : "2"));
        } else {
            System.out.println(
                "FARO MIRADOR --- " + Thread.currentThread().getName() + " se bajó del tobogán porque se cerró el parque.");
        }

        return abierto;
    }

    public void liberarTobogan(boolean valor) {
        if (valor) {
            tobogan1.release();
        } else {
            tobogan2.release();
        }
        System.out.println(ANSI_GREEN + "FARO MIRADOR --- " + Thread.currentThread().getName() + " se bajó del tobogan "
                + (valor ? "1" : "2") + ANSI_RESET);
    }

    public void ocuparEscalon(Visitante visitante) {
        try {
            escalera.add(visitante);
            System.out.println(ANSI_RED + "FARO MIRADOR --- " + Thread.currentThread().getName()
                    + " entró a la escalera." + ANSI_RESET);
        } catch (IllegalStateException e) {
            System.out.println("FARO MIRADOR --- Escalera llena");
        }
    }

    public boolean desocuparEscalon(Visitante visitante) {
        boolean rta = escalera.remove(visitante);
        if (rta) {
            System.out.println("FARO MIRADOR --- " + Thread.currentThread().getName() + " dejó la escalera.");
        } else {
            System.out.println("FARO MIRADOR --- " + Thread.currentThread().getName() + " dejó la escalera porque el parque cerró.");   
        }
        return rta;
    }

    public void cerrar() {
        abierto = false;
        escalera.clear();
        tobogan1.release(100);
        tobogan2.release(100);
    }
}
