package Recursos;

public class Horario {
    private int hora = 9;
    private int minuto = 0;

    public int getHora() {
        return hora;
    }

    public int getMinuto() {
        return minuto;
    }

    public void aumentarTiempo() {
        if (minuto == 60) {
            minuto = 0;
            hora++;
        } else {
            minuto++;
        }
    }
}
