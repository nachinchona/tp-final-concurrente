package Hilos;
import java.util.Set;

import Recursos.EcoParque;
import Recursos.Interfaz;

public class Reloj extends Thread {
    private Interfaz horario;
    private EcoParque parque;

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[46m";

    public Reloj(Interfaz horario, EcoParque parque) {
        this.horario = horario;
        this.parque = parque;
    }

    public void run() {
        // 500 milisegundos equivale a un minuto
        while (true) {
            horario.aumentarTiempo();
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (horario.getHora() % 2 == 0 && horario.getMinuto() == 0) {
                parque.getNadoDelfines().avisarControl();
            }
            if (horario.getHora() % 2 == 0 && horario.getMinuto() == 45) {
                parque.getNadoDelfines().avisarControl();
            }
            if (horario.getHora() == 18 && horario.getMinuto() == 00) {
                System.out.println(ANSI_CYAN + "------------------------------ PARQUE CERRADO ------------------------------" + ANSI_RESET);
                parque.getGomones().cerrar();
            }
            if (horario.getHora() > 20 && horario.getMinuto() == 00) {
                /* Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                System.out.println(threadSet); */
                parque.getGomones().imprimirEstado();
            }
        }
        
    }
}
