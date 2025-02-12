package Recursos;

public class Pileta {
    private final int CAPACIDAD = 10;
    private boolean piletaOcupada = false;
    private int cantidadActual = 0;

    public synchronized boolean ingresar() {
        if (cantidadActual < CAPACIDAD) {
            cantidadActual++;
        } else if (cantidadActual == CAPACIDAD) {
            piletaOcupada = true;
        }
        return piletaOcupada;
    }

    public synchronized boolean salir() {
        cantidadActual--;
        if (cantidadActual == 0) {
            piletaOcupada = false;
        }
        return piletaOcupada;
    }

    public boolean estaOcupada() {
        return piletaOcupada;
    }
}
