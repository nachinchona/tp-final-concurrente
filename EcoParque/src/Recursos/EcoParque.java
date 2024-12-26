package Recursos;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;

import Hilos.Reloj;
import Hilos.Visitante;
import Actividades.CarreraGomones.Chofer;
import Actividades.FaroMiradorTobogan.Administrador;
import Actividades.Ingreso.Colectivero;
import Actividades.Ingreso.Ingreso;
import Actividades.NadoDelfines.NadoDelfines;
import Actividades.Snorkel.Asistente;
import Actividades.Tienda.Cajero;

public class EcoParque {
    Random r = new Random();
    Interfaz interfaz = new Interfaz();
    Reloj reloj = new Reloj(interfaz, this);

    // actividades e ingreso
    Ingreso ingreso = new Ingreso(interfaz);
    Tienda tienda = new Tienda();
    NadoDelfines nado = new NadoDelfines();
    FaroMirador faro = new FaroMirador(this);
    EquipoSnorkel snorkel = new EquipoSnorkel(this);
    Restaurante[] restaurantes = new Restaurante[2];
    Gomones gomones = new Gomones();

    // empleados y controles
    Cajero cajero1 = new Cajero(this);
    Cajero cajero2 = new Cajero(this);
    Asistente asistenteSnorkel1 = new Asistente(this);
    Asistente asistenteSnorkel2 = new Asistente(this);
    Administrador administradorFaro = new Administrador(this);
    Colectivero colectivero = new Colectivero(this);
    Chofer chofer = new Chofer(this);

    public EcoParque() {
        restaurantes[0] = new Restaurante("Restaurante");
        restaurantes[1] = new Restaurante("Etnaruatser");
    }

    public void abrir() {
        if (interfaz.getHora() >= 9) {
            reloj.start();
            cajero1.start();
            cajero2.start();
            administradorFaro.start();
            asistenteSnorkel1.start();
            asistenteSnorkel2.start();
            colectivero.start();
            // reloj visual
            interfaz.iniciarInterfaz();    
        }
    }

    public void irAFaroMirador(Visitante visitante) throws InterruptedException {
        faro.ocuparEscalon(visitante);
        Thread.sleep(r.nextInt(1000));
        boolean rta = faro.desocuparEscalon(visitante);
        if (rta) {
            faro.avisarAdministrador();
            boolean toboganAsignado = faro.esperarAviso();
            faro.usarTobogan(toboganAsignado);
            Thread.sleep(r.nextInt(2000, 4000));
            faro.liberarTobogan(toboganAsignado);
        }
    }

    public void hacerSnorkel(Visitante visitante) throws InterruptedException {
        boolean rta = snorkel.hacerFila(visitante);
        Thread.sleep(r.nextInt(1000));
        if (rta) {
            snorkel.hacerSnorkel();
            Thread.sleep(r.nextInt(2000, 4000));
            snorkel.dejarEquipo();
        }
    }

    public void irARestaurante(int eleccion, Visitante visitante) throws InterruptedException {
        if (eleccion >= 0 && eleccion < restaurantes.length - 1) {
            restaurantes[eleccion].entrarRestaurante(visitante);
            if (r.nextBoolean()) {
                restaurantes[eleccion].almorzar(visitante);
            } else {
                restaurantes[eleccion].merendar(visitante);
            }
        }
    }

    public void hacerCarreraGomones(boolean eleccionTransporte, boolean eleccionGomon, Visitante visitante) throws InterruptedException, BrokenBarrierException {
        // eleccionTransporte true = bicicleta, false = tren
        gomones.elegirTransporte(eleccionTransporte, visitante);
        gomones.guardarBolso(visitante);
        gomones.realizarActividad(eleccionGomon, visitante);
        gomones.guardarBolso(visitante);
        if (!eleccionTransporte) {
            gomones.bajarTren();
        }
    }

    public void ingresar(boolean eleccion) throws InterruptedException {
        if (eleccion) {
            // por tour
            ingreso.ingresarPorTour();
            Thread.sleep(r.nextInt(500,1000));
            ingreso.entrarMolinete();
        } else {
            // particular
            ingreso.entrarMolinete();
        }
        interfaz.aumentarVisitantes();
    }

    public void salir() {
        interfaz.decrementarVisitantes();
    }

    public void realizarActividad(int eleccion, Visitante visitante) throws InterruptedException {
        if (sePuedenRealizarActividades()) {
            switch (eleccion) {
                case 0:
                    hacerSnorkel(visitante);
                    break;
                case 1:
                    irAFaroMirador(visitante);
                    break;
                case 2:
                    irARestaurante(r.nextInt(2), visitante);
                    break;
                case 3:

                    break;
                case 4:

                    break;
                default:
                    break;
            }
        }
    }

    public void comprarEnTienda() throws InterruptedException {
        tienda.comprar();
    }

    public boolean estaAbierto() {
        int hora = interfaz.getHora();
        return 9 <= hora && hora < 17;
    }

    public boolean sePuedenRealizarActividades() {
        int hora = interfaz.getHora();
        return 9 <= hora && hora < 18;
    }

    public EquipoSnorkel getEquipoSnorkel() {
        return snorkel;
    }

    public NadoDelfines getNadoDelfines() {
        return nado;
    }

    public Tienda getTienda() {
        return tienda;
    }

    public FaroMirador getFaroMirador() {
        return faro;
    }

    public Ingreso getIngreso() {
        return ingreso;
    }

    public Gomones getGomones() {
        return gomones;
    }
}
