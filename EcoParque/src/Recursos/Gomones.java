package Recursos;

import Actividades.CarreraGomones.Gomon;
import Actividades.CarreraGomones.GomonDoble;
import Actividades.CarreraGomones.GomonIndividual;
import Hilos.Visitante;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Gomones {
    private static final int CAPACIDAD_TREN = 15;
    private static final int CANTIDAD_BICICLETAS = 20;
    private static final int CANTIDAD_GOMONES_INDIVIDUALES = 10;
    private static final int CANTIDAD_GOMONES_DOBLES = 5;
    private static final int H = 5; // Número mínimo de gomones para iniciar la carrera
    private static final int TIEMPO_ESPERA = 10; // Tiempo máximo de espera (en segundos)
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[46m";

    private final Lock lockTren = new ReentrantLock(); // Lock para manejar pasajeros
    private final Condition trenListo = lockTren.newCondition(); // Notificación para la salida del tren
    private final Condition bajarTren = lockTren.newCondition();
    private final Condition volverTren = lockTren.newCondition();

    private int pasajerosActuales = 0; // Contador de pasajeros en el tren
    private boolean trenEnViaje = false; // Indica si el tren está en viaje

    private final Semaphore bicicletas = new Semaphore(CANTIDAD_BICICLETAS, true);

    private Lock lockCarrera = new ReentrantLock(true);
    private final Condition inicioCarrera = lockCarrera.newCondition();
    private final Condition esperandoCarrera = lockCarrera.newCondition();
    private final Condition esperandoCompañero = lockCarrera.newCondition();
    private final Condition esperaGomonInd = lockCarrera.newCondition();
    private final Condition esperaGomonDoble= lockCarrera.newCondition();
    private final HashMap<Visitante, Integer> bolsos = new HashMap<>();

    // Donde estan los gomones
    private BlockingQueue<GomonIndividual> disponiblesIndividuales = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> disponiblesDobles = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> esperandoDobles = new LinkedBlockingQueue<>();

    private int gomonesListos = 0;
    private int bolsoActual = 0;
    private int posiciones = 1;
    private boolean enCarrera = false;
    private String gomonesACorrer = "GOMONES LISTOS \n";
    private int cantEsperandoCarrera = 0;
    private int cantInicioCarrera = 0;
    private int cantEsperandoCompa = 0;
    private int cantEsperaGomonInd = 0;
    private int cantEsperaGomonDoble = 0;

    public Gomones() {
        for (int i = 0; i < CANTIDAD_GOMONES_INDIVIDUALES; i++) {
            disponiblesDobles.add(new GomonDoble(i + 1));
        }
        for (int i = CANTIDAD_GOMONES_INDIVIDUALES; i < CANTIDAD_GOMONES_DOBLES + CANTIDAD_GOMONES_INDIVIDUALES; i++) {
            disponiblesIndividuales.add(new GomonIndividual(i + 1));
        }
    }

    // cambie creo donde se actualizan los gomonesListos
    // igual se ve en git q digo juuuuuuua

    public void realizarActividad(boolean usaGomonDoble, Visitante visitante)
            throws InterruptedException, BrokenBarrierException {
        // Obtener un gomon
        lockCarrera.lock();
        Gomon miGomon;
        if (usaGomonDoble) {
            System.out.println("GOMONES --- " + Thread.currentThread().getName() + " obtiene un gomon doble.");
            if (!esperandoDobles.isEmpty()) {
                miGomon = esperandoDobles.take();
                ((GomonDoble) miGomon).añadirCompañero(visitante);
                while (gomonesListos >= H) {
                    esperandoCarrera.await();
                }
                gomonesListos++;
                gomonesACorrer += miGomon.toString() + "\n";
                esperandoCompañero.signalAll();
            } else {
                while (disponiblesDobles.isEmpty()) {
                    esperaGomonDoble.await();
                    cantEsperaGomonDoble--;
                }
                miGomon = disponiblesDobles.take();
                miGomon.añadirVisitante(visitante);
                esperandoDobles.add((GomonDoble) miGomon);
                System.out.println("GOMONES --- " + Thread.currentThread().getName() + " espera por un compañero.");
                while (((GomonDoble) miGomon).getSegundoVisitante() == null) {
                    esperandoCompañero.await();
                    cantEsperandoCompa--;
                }
            }
        } else {
            while (disponiblesIndividuales.isEmpty()) {
                esperaGomonInd.await();
            }
            miGomon = disponiblesIndividuales.take();
            miGomon.añadirVisitante(visitante);
            while (gomonesListos >= H) {
                esperandoCarrera.await();
                cantEsperandoCarrera--;
            }
            gomonesListos++;
            System.out.println("GOMONES --- " + Thread.currentThread().getName() + " obtiene un gomon individual.");
            gomonesACorrer += miGomon.toString() + "\n";
        }

        // Ya con gomon listo, espera a que haya H gomones listos. Si los hay, comienza la carrera
        if (gomonesListos == H) {
            enCarrera = true;
            inicioCarrera.signalAll();
            System.out.println(gomonesACorrer);
            gomonesACorrer = "GOMONES LISTOS \n";
        } else {
            while (!enCarrera) {
                esperandoCarrera.signalAll();
                inicioCarrera.await();
            }
        }

        System.out.println("GOMONES --- " + Thread.currentThread().getName() + " inicia la carrera.");
        lockCarrera.unlock();
        Thread.sleep(miGomon.correr());
        lockCarrera.lock();
        if (miGomon.esPrimerVisitante(visitante)) {
            switch (posiciones) {
                case 1:
                    System.out.println(ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString()
                            + " salió primero en la carrera!" + ANSI_RESET);
                    posiciones++;
                    break;
                case 2:
                    System.out.println(ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString()
                            + " salió segundo en la carrera!" + ANSI_RESET);
                    posiciones++;
                    break;
                case 3:
                    System.out.println(ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString()
                            + " salió tercero en la carrera!" + ANSI_RESET);
                    posiciones++;
                    break;
                case 5:
                    System.out.println(ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString() + " terminó la carrera."
                            + ANSI_RESET);
                    posiciones = 1;
                    gomonesListos = 0;
                    enCarrera = false;
                    esperandoCarrera.signalAll();
                    break;
                default:
                    System.out.println(ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString() + " terminó la carrera."
                            + ANSI_RESET);
                    posiciones++;
                    break;
            }

            if (usaGomonDoble) {
                disponiblesDobles.put((GomonDoble) miGomon);
                esperaGomonDoble.signalAll();
                ((GomonDoble) miGomon).reiniciar();
            } else {
                disponiblesIndividuales.put((GomonIndividual) miGomon);
                esperaGomonInd.signalAll();
            }
        }
        lockCarrera.unlock();
    }

    public void elegirTransporte(boolean eleccionTransporte, Visitante visitante) throws InterruptedException {
        // Traslado al inicio
        if (eleccionTransporte) {
            System.out.println(ANSI_RED + "GOMONES --- " + Thread.currentThread().getName()
                    + " usa una bicicleta para llegar." + ANSI_RESET);
            bicicletas.acquire();
            Thread.sleep(1000); // Simula el traslado
            bicicletas.release();
        } else {
            usarTren();
        }
    }

    public void guardarBolso(Visitante visitante) {
        // Guardar pertenencias
        bolsos.put(visitante, ++bolsoActual);
        System.out.println(
                "GOMONES --- " + Thread.currentThread().getName() + " guarda pertenencias en el bolso " + bolsoActual);
    }

    public void buscarBolso(Visitante visitante) {
        System.out.println(
                ANSI_GREEN + "GOMONES --- " + Thread.currentThread().getName()
                        + " termina la carrera y recoge su bolso "
                        + bolsos.get(visitante) + ANSI_RESET);
        bolsos.remove(visitante);
    }

    public void usarTren() throws InterruptedException {
        lockTren.lock();
        try {
            // Esperar si el tren está en viaje
            while (trenEnViaje || pasajerosActuales >= CAPACIDAD_TREN) {
                trenListo.await();
            }

            // Subir al tren
            pasajerosActuales++;
            System.out.println(
                    ANSI_RED + "GOMONES --- " + Thread.currentThread().getName()
                            + " abordó el tren. Pasajeros actuales: "
                            + pasajerosActuales + ANSI_RESET);

            // Si se llena el tren, notificar al conductor
            if (pasajerosActuales == CAPACIDAD_TREN) {
                trenListo.signalAll(); // Despertar al conductor para que salga
            }
        } finally {
            lockTren.unlock();
        }
    }

    public void volverTren() throws InterruptedException {
        System.out.println("GOMONES --- Conductor espera a que bajen todos los pasajeros");
        bajarTren.signalAll();
        volverTren.await();
        System.out.println("GOMONES --- Se bajaron todos los pasajeros, tren pega la vuelta...");
        Thread.sleep(5000);
        trenEnViaje = false;
        trenListo.signalAll();
    }

    public void bajarTren() {
        try {
            lockTren.lock();
            bajarTren.await();
            pasajerosActuales--;
            System.out.println(ANSI_GREEN + "GOMONES --- " + Thread.currentThread().getName()
                    + "se baja del tren. Pasajeros actuales: " + pasajerosActuales + ANSI_RESET);
            if (pasajerosActuales == 0) {
                volverTren.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockTren.unlock();
        }
    }

    public void controlTren() {
        while (true) {
            lockTren.lock();
            try {
                System.out.println("GOMONES --- Conductor está esperando completar o tiempo límite...");
                trenListo.await(TIEMPO_ESPERA, TimeUnit.SECONDS); // Esperar hasta llenar el tren o alcanzar el tiempo

                // Salida del tren
                if (pasajerosActuales > 0) {
                    System.out.println("GOMONES --- El tren sale con " + pasajerosActuales + " pasajeros.");
                    trenEnViaje = true;

                    // Simula el viaje
                    Thread.sleep(5000);

                    volverTren();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lockTren.unlock();
            }
        }
    }
}
