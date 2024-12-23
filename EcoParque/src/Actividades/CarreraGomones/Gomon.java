package Actividades.CarreraGomones;

import Hilos.Visitante;

import java.util.Random;

public abstract class Gomon {
    private int numeroGomon;
    private Visitante primerVisitante;
    private Random r = new Random();
    private int tiempo = r.nextInt(2000);

    public void añadirVisitante(Visitante visitante) {
        this.primerVisitante = visitante;
    }

    public int correr() {
        return tiempo;
    }
}
