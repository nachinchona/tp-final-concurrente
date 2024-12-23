package Actividades.CarreraGomones;

import Hilos.Visitante;

public class GomonDoble extends Gomon {
    private Visitante primerVisitante;
    private Visitante segundoVisitante;

    public void añadirVisitante(Visitante visitante) {
        super.añadirVisitante(visitante);
    }
    public void añadirCompañero(Visitante visitante) {
        this.segundoVisitante = visitante;
    }
}
