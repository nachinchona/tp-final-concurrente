package Actividades.CarreraGomones;

import Hilos.Visitante;

public class GomonDoble extends Gomon {
    private Visitante segundoVisitante;

    public GomonDoble(int numeroGomon) {
        super(numeroGomon);
    }

    public void añadirVisitante(Visitante visitante) {
        super.añadirVisitante(visitante);
    }

    public void añadirCompañero(Visitante visitante) {
        this.segundoVisitante = visitante;
    }

    public Visitante getSegundoVisitante() {
        return segundoVisitante;
    }

    public String toString() {
        return super.toString() + "-" + segundoVisitante.getName();
    }
}
