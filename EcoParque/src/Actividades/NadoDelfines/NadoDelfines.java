package Actividades.NadoDelfines;

import Recursos.Pileta;

public class NadoDelfines {
    private final Pileta[] piletas = new Pileta[4];
    private int piletasOcupadas = 0;

    public NadoDelfines() {
        for (int i = 0; i < piletas.length; i++) {
            piletas[i] = new Pileta();
        }
    }

    public synchronized Pileta buscarPileta() {
        Pileta pileta = null;
        int i = 0;
        boolean encontrado = false;
        while (!encontrado) {
            if (!piletas[i].estaOcupada()) {
                pileta = piletas[i];
                pileta.ingresar();
                encontrado = true;
            } else {
                piletasOcupadas++;
            }
        }
        return pileta;
    }

}
