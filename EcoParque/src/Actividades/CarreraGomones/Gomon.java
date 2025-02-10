package Actividades.CarreraGomones;

import Hilos.Visitante;

import java.util.Random;

public abstract class Gomon {
    private int numeroGomon;
    private Visitante primerVisitante;
    private Random r = new Random();
    private int tiempo = r.nextInt(1000,2000);

    public Gomon(int numeroGomon) {
        this.numeroGomon = numeroGomon;
    }

    public void añadirVisitante(Visitante visitante) {
        this.primerVisitante = visitante;
    }

    public int correr() {
        return tiempo;
    }

    public Visitante getPrimerVisitante() {
        return primerVisitante;
    }

    public int getNumeroGomon() {
        return numeroGomon;
    }

    public String toString() {
        return numeroGomon + "-" + (primerVisitante == null ? "":primerVisitante.getName());
    }

    public boolean esPrimerVisitante(Visitante visitante) {
        return this.primerVisitante.equals(visitante);
    }
}
