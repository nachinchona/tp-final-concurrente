package Recursos;

public class Pileta {
    private final int CAPACIDAD = 10;
    private boolean piletaOcupada = false;
    private int cantidadActual = 0;

    public synchronized void ingresar() {
        if (cantidadActual < CAPACIDAD) {
            cantidadActual++;
        } else {
            piletaOcupada = true;
        }
    }

    public synchronized void salir() {
        cantidadActual--;
        if (cantidadActual == 0) {
            piletaOcupada = false;
        }
    }

    public boolean estaOcupada() {
        return piletaOcupada;
    }
}
