package Recursos;

import java.util.Random;
import Hilos.Visitante;
import Actividades.NadoDelfines.NadoDelfines;
import Actividades.Snorkel.Asistente;

public class EcoParque {
    boolean estaAbierto = false;
    Horario horario = new Horario();

    NadoDelfines nado = new NadoDelfines();
    FaroMirador faro = new FaroMirador();
    EquipoSnorkel snorkel = new EquipoSnorkel();
    // demas actividades

    public void abrirParque() {
        estaAbierto = true;
        Asistente asistenteSnorkel = new Asistente(snorkel);
        asistenteSnorkel.start();
    }

    public void realizarActividad(Visitante visitante) throws InterruptedException {
        Random r = new Random();
        int siguienteActividad = r.nextInt(4);
        switch (siguienteActividad) {
            case 0:
                faro.ocuparEscalon(visitante);
                faro.desocuparEscalon(visitante);
                faro.avisarAdministrador();
                boolean toboganAsignado = faro.esperarAviso();
                faro.usarTobogan(toboganAsignado);
                faro.liberarTobogan(toboganAsignado);
                break;
            case 1:
                snorkel.hacerFila(visitante);
                snorkel.salirFila(visitante);
                snorkel.recibirEquipo();
                snorkel.dejarEquipo();
                break;
            case 2:

                break;
            case 3:

                break;
            case 4:

                break;
            default:
                break;
        }
    }

    public Horario getHorario() {
        return horario;
    }
}
