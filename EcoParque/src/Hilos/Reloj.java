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
        while (true) {
            horario.aumentarTiempo();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
