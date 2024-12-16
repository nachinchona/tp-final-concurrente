package Recursos;

import Hilos.Visitante;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Restaurante {
    private String nombre;
    private final int capacidad = 20;
    private Semaphore fila = new Semaphore(capacidad, true);
    // hash map que almacena visitante y si ya comio/merendo ese dia
    private final HashMap<Visitante, Boolean[]> mapa = new HashMap<>();
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    public Restaurante(String nombre) {
        this.nombre = nombre;
    }

    public void entrarRestaurante(Visitante visitante) throws InterruptedException {
        fila.acquire();
        if (!mapa.containsKey(visitante)) {
            mapa.put(visitante, new Boolean[]{false, false});
        }
    }

    public void almorzar(Visitante visitante) throws InterruptedException {
        if (mapa.get(visitante)[0]) {
            System.out.println("RESTAURANTES --- " + visitante.getName() + " ya ha tenido su almuerzo.");
        } else {
            System.out.println(ANSI_RED + "RESTAURANTES --- " + visitante.getName() + " está almorzando en " + nombre + "." + ANSI_RESET);
            Thread.sleep(2000);
            System.out.println(ANSI_GREEN + "RESTAURANTES --- " + visitante.getName() + " ha terminado de almorzar en " + nombre + "." + ANSI_RESET);
            mapa.get(visitante)[0] = true;
        }
        fila.release();
    }

    public void merendar(Visitante visitante) throws InterruptedException {
        if (mapa.get(visitante)[1]) {
            System.out.println("RESTAURANTES --- " + visitante.getName() + " ya ha tenido su merienda.");
        } else {
            System.out.println(ANSI_RED + "RESTAURANTES --- " + visitante.getName() + " está merendando en " + nombre + "." + ANSI_RESET);
            Thread.sleep(2000);
            System.out.println(ANSI_GREEN + "RESTAURANTES --- " + visitante.getName() + " ha terminado de merendar en " + nombre + "." + ANSI_RESET);
            mapa.get(visitante)[1] = true;
        }
        fila.release();
    }

    public void cerrar() {
        mapa.clear();
    }
}

