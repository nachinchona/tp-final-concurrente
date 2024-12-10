package Hilos;
import Recursos.Horario;

public class Reloj extends Thread {
    private Horario horario;

    public Reloj(Horario horario) {
        this.horario = horario;
    }

    public void run() {
        // un milisegundo equivale a un minuto
        while (true) {
            horario.aumentarTiempo();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
