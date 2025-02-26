package Recursos;

public class Pileta {
    private final int CAPACIDAD = 10;
    private boolean piletaOcupada = false;
    private int cantidadActual = 0;

    public boolean ingresar() {
        if (cantidadActual < CAPACIDAD) {
            cantidadActual++;
            if (cantidadActual == CAPACIDAD) {
                piletaOcupada = true;
            }
        }
        return piletaOcupada;
    }

    public boolean salir() {
        if (cantidadActual > 0) {
            cantidadActual--;
            if (cantidadActual == 0) {
                piletaOcupada = false;
            }
        }
        return piletaOcupada;
    }

    public boolean estaOcupada() {
        return piletaOcupada;
    }
}
