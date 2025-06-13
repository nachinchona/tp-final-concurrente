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
    public static final String ANSI_PURPLE = "\033[0;35m";

    public Reloj(Interfaz horario, EcoParque parque) {
        this.horario = horario;
        this.parque = parque;
    }

    public void run() {
        // 120 milisegundos equivale a un minuto
        while (true) {
            horario.aumentarTiempo();
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (horario.getHora() == 16 && horario.getMinuto() == 0) {
                parque.getNadoDelfines().cerrar();
            }

            if (horario.getHora() <= 16 && horario.getHora() % 2 == 0 && horario.getMinuto() == 0) {
                parque.getNadoDelfines().avisarControl();
            }

            if (horario.getHora() <= 16 && horario.getHora() % 2 == 0 && horario.getMinuto() == 45) {
                parque.getNadoDelfines().avisarControl();
            }

            if (horario.getHora() == 17 && horario.getMinuto() == 45) {
                parque.cerrar();
                parque.cerrarActividades();
            }
            if (horario.getHora() == 18 && horario.getMinuto() == 0) {
                System.out.println(ANSI_PURPLE + "------------------------------ CIERRAN ACTIVIDADES ------------------------------" + ANSI_RESET);
            }

            if (horario.getHora() == 19 && horario.getMinuto() == 0) {
                System.out.println(ANSI_PURPLE + "------------------------------ CIERRA PARQUE ------------------------------" + ANSI_RESET);
                parque.cerrar();
            }

        }
        
    }
}
