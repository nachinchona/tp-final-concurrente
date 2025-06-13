package Recursos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class FaroMirador {
    private BlockingQueue<Visitante> escalera = new ArrayBlockingQueue<>(10);
    private EcoParque parque;
    private boolean abierto = true;
    private boolean tobogan1Libre = true;
    private boolean tobogan2Libre = true;
    private Semaphore tobogan1 = new Semaphore(1, true);
    private Semaphore tobogan2 = new Semaphore(1, true);
    private Semaphore administrador = new Semaphore(0, true);
    private Semaphore avisarVisitante = new Semaphore(0, true);
    private Semaphore mutexToboganes = new Semaphore(2);
    private Semaphore toboganes = new Semaphore(2, true);
    private boolean asignado = false; // tobogan asignado por el administrador | TRUE PARA TOBOGAN 1 Y FALSE PARA EL 2
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    public FaroMirador(EcoParque parque) {
        this.parque = parque;
    }

    public void avisarVisitante() {
        avisarVisitante.release();
    }

    public void asignarTobogan() throws InterruptedException {
        administrador.acquire();
        if (abierto) {
            mutexToboganes.acquire();
            if (tobogan1Libre) {
                asignado = true;
            } else if (tobogan2Libre) {
                asignado = false;
            }
            mutexToboganes.release();
        }
    }

    public void usarTobogan(boolean valor) throws InterruptedException {
        if (!abierto) return;
        mutexToboganes.acquire();
        if (valor) {
            tobogan1.acquire();
            tobogan1Libre = false;
        } else {
            tobogan2.acquire();
            tobogan2Libre = false;
        }
        System.out.println("FARO MIRADOR --- " + Thread.currentThread().getName() + " se subió al tobogán " + (valor ? "1" : "2"));
    }

    public boolean ocuparEscalon(Visitante visitante) {
        boolean pudoEntrar = false;
        if (abierto) {
            try {
                escalera.add(visitante);
            /*System.out.println(ANSI_RED + "FARO MIRADOR --- " + Thread.currentThread().getName()
                    + " entró a la escalera." + ANSI_RESET);*/
                pudoEntrar = true;
            } catch (IllegalStateException e) {
                //System.out.println("FARO MIRADOR --- Escalera llena");
            }
        }
        return pudoEntrar;
    }

    public void avisarAdministrador() {
        administrador.release();
    }

    public boolean esperarAviso() throws InterruptedException {
        if (abierto) {
            avisarVisitante.acquire();
        }
        return asignado;
    }

    public boolean desocuparEscalon(Visitante visitante) {
        boolean irse;
        if (escalera.remove(visitante)) {
            System.out.println("FARO MIRADOR --- " + Thread.currentThread().getName() + " dejó la escalera.");
            irse = false;
        } else {
            System.out.println("FARO MIRADOR --- " + Thread.currentThread().getName() + " dejó la escalera porque el parque cerró.");
            irse = true;
        }
        return irse;
    }

    public void liberarTobogan(boolean valor) throws InterruptedException {
        if (valor) {
            tobogan1Libre = true;
            tobogan1.release();
        } else {
            tobogan2Libre = true;
            tobogan2.release();
        }
        System.out.println(ANSI_GREEN + "FARO MIRADOR --- " + Thread.currentThread().getName() + " se bajó del tobogan "
                + (valor ? "1" : "2") + ANSI_RESET);
        mutexToboganes.release();
    }

    public void cerrar() {
        abierto = false;
        escalera.clear();
        avisarVisitante.release(10);
        mutexToboganes.release(2);
    }
}
