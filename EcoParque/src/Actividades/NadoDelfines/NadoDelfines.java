package Actividades.NadoDelfines;

import Recursos.Pileta;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NadoDelfines {
    private final Pileta[] piletas = new Pileta[4];
    private final Lock lockPileta = new ReentrantLock();
    private final Condition esperarAviso = lockPileta.newCondition();
    private final Condition esperarHorario = lockPileta.newCondition();
    private final Condition esperarPiletasVacias = lockPileta.newCondition();
    private final Condition salirPileta = lockPileta.newCondition();
    private int piletasOcupadas = 0;
    private int piletasVacias = piletas.length;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[46m";

    private boolean sePuedeNadar = false;

    public NadoDelfines() {
        for (int i = 0; i < piletas.length; i++) {
            piletas[i] = new Pileta();
        }
    }

    public Pileta buscarPileta() {
        lockPileta.lock();
        Pileta pileta = null;
        int i = 0;
        boolean encontrado = false;
        while (!encontrado && !sePuedeNadar && i < piletas.length) {
            if (!piletas[i].estaOcupada()) {
                pileta = piletas[i];
                boolean rta = pileta.ingresar();
                if (rta) {
                    piletasOcupadas++;
                    piletasVacias--;
                }
                encontrado = true;
            }
            i++;
        }
        if (encontrado) {
            System.out.println(ANSI_RED + "NADO DELFINES --- " + Thread.currentThread().getName() + " encontró pileta." + ANSI_RESET);
        }
        lockPileta.unlock();
        return pileta;
    }

    public void salirPileta(Pileta pileta) throws InterruptedException {
        lockPileta.lock();
        if (!pileta.salir()) {
            piletasOcupadas--;
            piletasVacias++;
            if (piletasVacias == piletas.length) {
                esperarPiletasVacias.signal();
            }
        }
        lockPileta.unlock();
    }

    public void nadar() throws InterruptedException {
        lockPileta.lock();
        esperarAviso.await();
        if (sePuedeNadar) {
            // Solo si se cumple el cupo
            salirPileta.await();
        }
        lockPileta.unlock();
    }

    public void avisarControl() {
        lockPileta.lock();
        esperarHorario.signal();
        lockPileta.unlock();
    }

    public boolean getSePuedeNadar() {
        return sePuedeNadar;
    }

    public void esperarPiletasLlenas() throws InterruptedException {
        lockPileta.lock();
        esperarHorario.await();
        // Espera hasta el horario de la actividad
        // Una vez que llega el horario avisa al resto que pueden nadar
        if (piletasOcupadas >= 3) {
            sePuedeNadar = true;
            esperarAviso.signalAll();
            System.out.println(ANSI_RED + "NADO DELFINES --- Comienza la actividad." + ANSI_RESET);
            // Espera a que llegue el fin de la actividad
            esperarHorario.await();
            salirPileta.signalAll();
            System.out.println(ANSI_GREEN + "NADO DELFINES --- Termina la actividad." + ANSI_RESET);
        } else {
            System.out.println("NADO DELFINES --- Cupo no alcanzado, no comienza la actividad.");
            esperarHorario.await();
        }
        lockPileta.unlock();
    }

    public void esperarPiletasVacias() throws InterruptedException {
        lockPileta.lock();
        if (piletasOcupadas >= 3) {
            esperarPiletasVacias.await();
            System.out.println("NADO DELFINES --- Piletas vacías, pueden ingresar los visitantes.");
            sePuedeNadar = false;
        }
        lockPileta.unlock();
    }
}
