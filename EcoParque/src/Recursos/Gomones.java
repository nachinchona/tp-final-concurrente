package Recursos;

import Actividades.CarreraGomones.Gomon;
import Actividades.CarreraGomones.GomonDoble;
import Actividades.CarreraGomones.GomonIndividual;
import Hilos.Visitante;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Gomones {
    private static final int CAPACIDAD_TREN = 15;
    private static final int CANTIDAD_BICICLETAS = 20;
    private static final int CANTIDAD_GOMONES_INDIVIDUALES = 15;
    private static final int CANTIDAD_GOMONES_DOBLES = 15;
    private static final int H = 5; // Número mínimo de gomones para iniciar la carrera
    private static final int TIEMPO_ESPERA = 10; // Tiempo máximo de espera (en segundos)
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[46m";

    private final Lock lockTren = new ReentrantLock(); // Lock para manejar pasajeros
    private final Condition trenListo = lockTren.newCondition(); // Notificación para la salida del tren
    private int trenListoI = 0;
    private final Condition bajarTren = lockTren.newCondition();
    private int bajarTrenI = 0;
    private final Condition volverTren = lockTren.newCondition();
    private int volverTrenI = 0;

    private int pasajerosActuales = 0; // Contador de pasajeros en el tren
    private boolean trenEnViaje = false; // Indica si el tren está en viaje
    private volatile boolean abierto = true;

    private final Semaphore bicicletas = new Semaphore(CANTIDAD_BICICLETAS, true);

    private Lock lockCarrera = new ReentrantLock(true);
    private final Condition inicioCarrera = lockCarrera.newCondition();
    private int inicioCar = 0;
    private final Condition esperandoCarrera = lockCarrera.newCondition();
    private int esperandoCar = 0;
    private final Condition esperandoCompañero = lockCarrera.newCondition();
    private int esperandoCom = 0;
    private final Condition esperaGomonInd = lockCarrera.newCondition();
    private int esperandoGomonInd = 0;
    private final Condition esperaGomonDoble = lockCarrera.newCondition();
    private int esperandoGomonDoble = 0;
    private final HashMap<Visitante, Integer> bolsos = new HashMap<>();

    // Donde estan los gomones
    private BlockingQueue<GomonIndividual> disponiblesIndividuales = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> disponiblesDobles = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> esperandoDobles = new LinkedBlockingQueue<>();
    private Queue<Gomon> gomonesListosQ = new ConcurrentLinkedQueue<>();

    private int gomonesListos = 0;
    private int bolsoActual = 0;
    private int posiciones = 1;
    private boolean enCarrera = false;
    private String gomonesACorrer = "GOMONES LISTOS \n";
    private CyclicBarrier inicioCarreraCL = new CyclicBarrier(H, () -> {
        System.out.println("GOMONES LISTOS \n");
        for (Gomon gomon : gomonesListosQ) {
            System.out.println(gomon.toString());
        }
        enCarrera = true;
    });

    private EcoParque parque;

    public Gomones(EcoParque parque) {
        this.parque = parque;
        for (int i = 0; i < CANTIDAD_GOMONES_INDIVIDUALES; i++) {
            disponiblesDobles.add(new GomonDoble(i + 1));
        }
        for (int i = CANTIDAD_GOMONES_INDIVIDUALES; i < CANTIDAD_GOMONES_DOBLES + CANTIDAD_GOMONES_INDIVIDUALES; i++) {
            disponiblesIndividuales.add(new GomonIndividual(i + 1));
        }
    }

    public boolean elegirTransporte(boolean eleccionTransporte, Visitante visitante) throws InterruptedException {
        // Traslado al inicio
        boolean seSubio = true;
        if (parque.sePuedenRealizarActividades()) {
            if (eleccionTransporte) {
                System.out.println(ANSI_RED + "GOMONES --- " + Thread.currentThread().getName()
                        + " usa una bicicleta para llegar." + ANSI_RESET);
                bicicletas.acquire();
                Thread.sleep(1000); // Simula el traslado
                bicicletas.release();
            } else {
                seSubio = usarTren();
            }
        }
        return seSubio;
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

    public boolean usarTren() throws InterruptedException {
        boolean seSubio = false;
        lockTren.lock();
        try {
            if (abierto) {
                if (!trenEnViaje && pasajerosActuales < CAPACIDAD_TREN) {
                    seSubio = true;
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
                }
            } else {
                System.out.println(" SUPER XD ASMDOAS D");
                return seSubio;
            }
        } finally {
            lockTren.unlock();
        }
        return seSubio;
    }

    public void volverTren() throws InterruptedException {
        System.out.println("GOMONES --- Conductor espera a que bajen todos los pasajeros.");
        bajarTren.signalAll();
        volverTrenI++;
        volverTren.await();
        volverTrenI--;
        System.out.println("GOMONES --- Se bajaron todos los pasajeros, tren pega la vuelta...");
        Thread.sleep(5000);
        trenEnViaje = false;
        trenListo.signalAll();
    }

    public void bajarTren() {
        try {
            if (abierto) {
                lockTren.lock();
                bajarTrenI++;
                bajarTren.await();
                bajarTrenI--;
                pasajerosActuales--;
                System.out.println(ANSI_GREEN + "GOMONES --- " + Thread.currentThread().getName()
                        + " se baja del tren. Pasajeros actuales: " + pasajerosActuales + ANSI_RESET);
                if (pasajerosActuales == 0) {
                    volverTren.signal();
                }
            } else {
                System.out.println("SOY VELOZ  ASDAS AFDS FDS ");
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockTren.unlock();
        }
    }

    public void controlTren() {
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

    public void cerrar() {
        abierto = false;

        /*
         * lockCarrera.lock();
         * inicioCarrera.signalAll();
         * esperaGomonDoble.signalAll();
         * esperaGomonInd.signalAll();
         * esperandoCarrera.signalAll();
         * esperandoCompañero.signalAll();
         * lockCarrera.unlock();
         */

    }

    public void imprimirEstado() {
        System.out.println(inicioCar + " <- inicioCarrera");
        System.out.println(esperandoCar + " <- esperandoCarrera");
        System.out.println(esperandoCom + " <- esperandoCompanero");
        System.out.println(esperandoGomonDoble + " <- esperaGomonDoble");
        System.out.println(esperandoGomonInd + " <- esperaGomonIndv");
        System.out.println(volverTrenI + " <- volverTren");
        System.out.println(bajarTrenI + " <- bajarTren");
        System.out.println(trenListoI + " <- trenListo");
        System.out.println(abierto + " <- abierto");
    }

    public void realizarActividad(boolean usaGomonDoble, Visitante visitante)
            throws InterruptedException {
        Gomon miGomon = null;
        try {
            lockCarrera.lock();
            if (usaGomonDoble) {
                System.out.println("GOMONES --- " + Thread.currentThread().getName() + " obtiene un gomon doble.");
                if (!esperandoDobles.isEmpty()) {
                    miGomon = esperandoDobles.poll();
                    ((GomonDoble) miGomon).añadirCompañero(visitante);
                    esperandoCar++;
                    while (gomonesListos >= H) {
                        esperandoCarrera.await(4, TimeUnit.SECONDS);
                        if (!abierto) {
                            System.out.println("                                              XD");
                            esperandoCar--;
                            return;
                        }
                    }
                    esperandoCar--;
                    gomonesListos++;
                    esperandoCompañero.signalAll();
                } else {
                    esperandoGomonDoble++;
                    while (disponiblesDobles.isEmpty()) {
                        esperaGomonDoble.await();
                    }
                    esperandoGomonDoble--;
                    miGomon = disponiblesDobles.poll(4000, TimeUnit.MILLISECONDS);
                    if (miGomon == null) {
                        System.out.println("GOMONES --- " + Thread.currentThread().getName()
                                + " se retira ya que no hay gomones dobles.");
                        return;
                    }
                    miGomon.añadirVisitante(visitante);
                    esperandoDobles.add((GomonDoble) miGomon);
                    System.out.println("GOMONES --- " + Thread.currentThread().getName() + " espera por un compañero.");
                    esperandoCom++;
                    while (((GomonDoble) miGomon).getSegundoVisitante() == null) {
                        esperandoCompañero.await(4, TimeUnit.SECONDS);
                        if (!abierto) {
                            System.out.println("                                              XD");
                            esperandoCom--;
                            return;
                        }
                    }
                    esperandoCom--;
                    System.out.println("GOMONES --- " + Thread.currentThread().getName() + " consigue compañero "
                            + ((GomonDoble) miGomon).getSegundoVisitante().toString() + ".");
                }
            } else {
                esperandoGomonInd++;
                while (disponiblesIndividuales.isEmpty()) {
                    esperaGomonInd.await();
                }
                esperandoGomonInd--;
                miGomon = disponiblesIndividuales.poll(4000, TimeUnit.MILLISECONDS);
                if (miGomon == null) {
                    System.out.println("GOMONES --- " + Thread.currentThread().getName()
                            + " se retira ya que no hay gomones individuales.");
                    return;
                }
                miGomon.añadirVisitante(visitante);
                esperandoCar++;
                while (gomonesListos >= H) {
                    esperandoCarrera.await(4, TimeUnit.SECONDS);
                    if (!abierto) {
                        System.out.println("                                              XD");
                        esperandoCar--;
                        return;
                    }
                }
                esperandoCar--;
                gomonesListos++;
                System.out.println("GOMONES --- " + Thread.currentThread().getName() + " obtiene un gomon individual.");
            }

            // Ya con gomon listo, espera a que haya H gomones listos. Si los hay, comienza
            // la carrera
            if (gomonesListos == H) {
                enCarrera = true;
                inicioCarrera.signalAll();
            } else {
                inicioCar++;
                while (!enCarrera) {
                    esperandoCarrera.signalAll();
                    inicioCarrera.await();
                }
                inicioCar--;
            }
            lockCarrera.unlock();

            System.out.println("GOMONES --- " + Thread.currentThread().getName() + " inicia la carrera.");
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
                        System.out.println(
                                ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString() + " terminó la carrera."
                                        + ANSI_RESET);
                        posiciones = 1;
                        gomonesListos = 0;
                        enCarrera = false;
                        esperandoCarrera.signalAll();
                        break;
                    default:
                        System.out.println(
                                ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString() + " terminó la carrera."
                                        + ANSI_RESET);
                        posiciones++;
                        break;
                }
            }
        } finally {
            if (miGomon != null) {
                if (usaGomonDoble) {
                    ((GomonDoble) miGomon).reiniciar();
                    disponiblesDobles.put((GomonDoble) miGomon);
                    esperaGomonDoble.signalAll();
                } else {
                    disponiblesIndividuales.put((GomonIndividual) miGomon);
                    esperaGomonInd.signalAll();
                }
            }
            lockCarrera.unlock();
        }
    }
}