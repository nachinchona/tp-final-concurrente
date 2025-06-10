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
    private final Semaphore compañero = new Semaphore(0, true);


    private Lock lockGomon = new ReentrantLock(true);
    private Lock lockCarrera = new ReentrantLock(true);
    private final Condition inicioCarrera = lockCarrera.newCondition();
    private int inicioCar = 0;
    private final Condition esperandoCarrera = lockCarrera.newCondition();
    private int esperandoCar = 0;
    private final Condition esperandoCompañero = lockGomon.newCondition();
    private int esperandoCom = 0;
    private final Condition esperaGomonInd = lockGomon.newCondition();
    private int esperandoGomonInd = 0;
    private final Condition esperaGomonDoble = lockGomon.newCondition();
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
                //System.out.println(" SUPER XD ASMDOAS D");
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
         lockCarrera.lock();
         abierto = false;
         esperandoCarrera.signalAll();
         lockCarrera.unlock();

    }

    public void imprimirEstado() {
        System.out.println(esperandoCar + " <- ESPERANDO A QUE CARRERA TERMINE");
        System.out.println(esperandoGomonDoble + " <- Esperando en lock del gomon");
        //System.out.println(esperandoGomonInd + " <- esperaGomonIndv");
        System.out.println(volverTrenI + " <- volverTren");
        System.out.println(bajarTrenI + " <- bajarTren");
        System.out.println(trenListoI + " <- trenListo");
        System.out.println(abierto + " <- abierto");
    }

    public Gomon subirGomon(boolean usaGomonDoble, Visitante visitante) throws InterruptedException {
        Gomon miGomon;

        if (usaGomonDoble) {
            boolean hayEsperando;

            lockGomon.lock();
            try {
                hayEsperando = !esperandoDobles.isEmpty();
            } finally {
                lockGomon.unlock();
            }

            if (hayEsperando) {
                //Si hay personas esperando
                miGomon = esperandoDobles.poll(8000, TimeUnit.MILLISECONDS);
                if (miGomon == null) {
                    System.out.println("Tiempo de espera agotado, me retiro: " + Thread.currentThread().getName());
                    lockGomon.lock();
                    return null;
                }
            } else {
                // Tomo un gomon doble para esperar compañero
                miGomon = disponiblesDobles.poll(8000, TimeUnit.MILLISECONDS);
                if (miGomon == null) {
                    System.out.println("Tiempo de espera agotado, me retiro: " + Thread.currentThread().getName());
                    return null;
                }
                lockGomon.lock();
                try {
                    esperandoDobles.add((GomonDoble) miGomon);
                } finally {
                    lockGomon.unlock();
                }
            }
        } else {
            miGomon = disponiblesIndividuales.poll(8000, TimeUnit.MILLISECONDS);
            if (miGomon == null) {
                System.out.println("GOMONES --- " + Thread.currentThread().getName()+ " se retira ya que no hay gomones individuales.");
                return null;
            }
            miGomon.añadirVisitante(visitante);
        }

        if (usaGomonDoble) {
            esperandoGomonDoble++;
            ((GomonDoble) miGomon).subirAlGomon(visitante);
            esperandoGomonDoble--;
        }
        return miGomon;
    }


    public void devolverGomon(Gomon miGomon, boolean usaGomonDoble) throws InterruptedException {
        if (miGomon != null) {
            if (usaGomonDoble) {
                ((GomonDoble) miGomon).reiniciar();
                lockGomon.lock();
                disponiblesDobles.put((GomonDoble) miGomon);
                System.out.println(disponiblesDobles.toString());
                esperaGomonDoble.signalAll();
                lockGomon.unlock();
            } else {
                ((GomonIndividual) miGomon).reiniciar();
                lockGomon.lock();
                disponiblesIndividuales.put((GomonIndividual) miGomon);
                System.out.println(disponiblesIndividuales.toString());
                esperaGomonInd.signalAll();
                lockGomon.unlock();
            }
        }
    }


    public void realizarActividad(Gomon miGomon, boolean usaGomonDoble, Visitante visitante)
            throws InterruptedException {
        if (miGomon != null) {
            if (miGomon.esPrimerVisitante(visitante)) {
                try {
                    lockCarrera.lock();
                    esperandoCar++;
                    while (enCarrera) {
                        esperandoCarrera.await();
                        if (!abierto) {
                            esperandoCar--;
                            return;
                        }
                    }
                    esperandoCar--;
                    gomonesListos++;
                    if (gomonesListos == 5) {
                        enCarrera = true;
                        inicioCarrera.signalAll();
                    } else {
                        inicioCarrera.await();
                    }
                    lockCarrera.unlock();
                    System.out.println(Thread.currentThread().getName() + " inicia la carrera");
                    Thread.sleep(miGomon.correr());
                    lockCarrera.lock();
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
                            System.out.println("AVISO QUE TERMINO");
                            esperandoCarrera.signalAll();
                            break;
                        default:
                            System.out.println(
                                    ANSI_CYAN + "GOMONES --- El gomón " + miGomon.toString() + " terminó la carrera."
                                            + ANSI_RESET);
                            posiciones++;
                            break;
                    }
                } finally {
                    lockCarrera.unlock();
                }
                if (usaGomonDoble) {
                    ((GomonDoble) miGomon).avisar();
                } else {
                    devolverGomon(miGomon, false);
                }
            } else {
                ((GomonDoble) miGomon).esperar();
                devolverGomon(miGomon, true);
            }
        }
    }
}