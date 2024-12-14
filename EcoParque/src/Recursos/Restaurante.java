package Recursos;

import Hilos.Visitante;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Restaurante {
    private String nombre;
    private final int capacidad = 20;
    private Semaphore fila = new Semaphore(capacidad, true);
    private final HashMap<Visitante, Boolean[]> mapa = new HashMap<>();

    public void entrarRestaurante(Visitante visitante) throws InterruptedException {
        fila.acquire();
        if (!mapa.containsKey(visitante)) {
            mapa.put(visitante, new Boolean[]{false, false});
        }
    }

    public void almuerzo(Visitante visitante) throws InterruptedException {
        if (mapa.get(visitante)[0]) {
            System.out.println(visitante.getName() + " ya ha tenido su almuerzo.");
        } else {
            System.out.println(visitante.getName() + " está almorzando en " + nombre + ".");
            Thread.sleep(2000); // Simular tiempo de almuerzo
            System.out.println(visitante.getName() + " ha terminado de almorzar en " + nombre + ".");
            mapa.get(visitante)[0] = true; // Marcar como almorzado
        }
        fila.release();
    }

    public void merienda(Visitante visitante) throws InterruptedException {
        if (mapa.get(visitante)[1]) {
            System.out.println(visitante.getName() + " ya ha tenido su almuerzo.");
        } else {
            System.out.println(visitante.getName() + " está almorzando en " + nombre + ".");
            Thread.sleep(2000); // Simular tiempo de merienda
            System.out.println(visitante.getName() + " ha terminado de almorzar en " + nombre + ".");
            mapa.get(visitante)[1] = true; // Marcar como que merendó
        }
        fila.release();
    }

}

