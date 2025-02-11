package Hilos;
import Recursos.EcoParque;
import Recursos.Interfaz;

public class Reloj extends Thread {
    private Interfaz horario;
    private EcoParque parque;

    public Reloj(Interfaz horario, EcoParque parque) {
        this.horario = horario;
        this.parque = parque;
    }

    public void run() {
        // 500 milisegundos equivale a un minuto
        while (parque.sePuedenRealizarActividades()) {
            horario.aumentarTiempo();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (horario.getHora() % 2 == 0 && horario.getMinuto() == 0) {
                parque.getNadoDelfines().notifyAll();
            }
            if (horario.getHora() % 2 == 0 && horario.getMinuto() == 4) {
                parque.getNadoDelfines().notifyAll();
            }
        }
        System.out.println("----- PARQUE CERRADO -----");
    }
}
