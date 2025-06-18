package Recursos;

import Actividades.CarreraGomones.Gomon;
import Actividades.CarreraGomones.GomonDoble;
import Actividades.CarreraGomones.GomonIndividual;
import Hilos.Visitante;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final Condition bajarTren = lockTren.newCondition();
    private final Condition volverTren = lockTren.newCondition();

    private int pasajerosActuales = 0; // Contador de pasajeros en el tren
    private boolean trenEnViaje = false; // Indica si el tren está en viaje
    private volatile boolean abierto = true;

    private final Semaphore bicicletas = new Semaphore(CANTIDAD_BICICLETAS, true);

    private Lock lockGomonDoble = new ReentrantLock(true);
    private Lock lockGomonIndividual = new ReentrantLock(true);
    private Lock lockCarrera = new ReentrantLock(true);
    private final Condition inicioCarrera = lockCarrera.newCondition();
    private final Condition esperandoCarrera = lockCarrera.newCondition();
    private final HashMap<Visitante, Integer> bolsos = new HashMap<>();

    // Donde estan los gomones
    private BlockingQueue<GomonIndividual> disponiblesIndividuales = new LinkedBlockingQueue<>();
    List<GomonDoble> todosLosDobles = new ArrayList<GomonDoble>();
    private BlockingQueue<GomonDoble> disponiblesDobles = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> esperandoDobles = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> esperandoCompañero = new LinkedBlockingQueue<>();
    private int gomonesListos = 0;
    private int bolsoActual = 0;
    private int posiciones = 1;
    private boolean enCarrera = false;

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
                /*System.out.println(ANSI_RED + "GOMONES --- " + Thread.currentThread().getName()
                        + " usa una bicicleta para llegar." + ANSI_RESET);*/
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
        /*System.out.println(
                "GOMONES --- " + Thread.currentThread().getName() + " guarda pertenencias en el bolso " + bolsoActual);*/
    }

    public void buscarBolso(Visitante visitante) {
        /*System.out.println(
                ANSI_GREEN + "GOMONES --- " + Thread.currentThread().getName()
                        + " termina la carrera y recoge su bolso "
                        + bolsos.get(visitante) + ANSI_RESET);*/
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
                    /*System.out.println(
                            ANSI_RED + "GOMONES --- " + Thread.currentThread().getName()
                                    + " abordó el tren. Pasajeros actuales: "
                                    + pasajerosActuales + ANSI_RESET);*/

                    // Si se llena el tren, notificar al conductor
                    if (pasajerosActuales == CAPACIDAD_TREN) {
                        trenListo.signalAll(); // Despertar al conductor para que salga
                    }
                }
            } else {
                return seSubio;
            }
        } finally {
            lockTren.unlock();
        }
        return seSubio;
    }

    public void volverTren() throws InterruptedException {
        //System.out.println("GOMONES --- Conductor espera a que bajen todos los pasajeros.");
        bajarTren.signalAll();
        volverTren.await();
        //System.out.println("GOMONES --- Se bajaron todos los pasajeros, tren pega la vuelta...");
        Thread.sleep(5000);
        trenEnViaje = false;
        trenListo.signalAll();
    }

    public void bajarTren() {
        try {
            if (abierto) {
                lockTren.lock();
                bajarTren.await();
                pasajerosActuales--;
                /*System.out.println(ANSI_GREEN + "GOMONES --- " + Thread.currentThread().getName()
                        + " se baja del tren. Pasajeros actuales: " + pasajerosActuales + ANSI_RESET);*/
                if (pasajerosActuales == 0) {
                    volverTren.signal();
                }
            } else {
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
            //System.out.println("GOMONES --- Conductor está esperando completar o tiempo límite...");
            trenListo.await(TIEMPO_ESPERA, TimeUnit.SECONDS); // Esperar hasta llenar el tren o alcanzar el tiempo

            // Salida del tren
            if (pasajerosActuales > 0) {
                //System.out.println("GOMONES --- El tren sale con " + pasajerosActuales + " pasajeros.");
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
         for (GomonDoble gomon : esperandoDobles) {
             gomon.interrumpirEspera();
         }
        for (GomonDoble gomon : esperandoCompañero) {
            gomon.interrumpirEspera();
        }
         esperandoDobles.clear();
         esperandoCompañero.clear();
         esperandoCarrera.signalAll();
         lockCarrera.unlock();
    }

    public GomonDoble subirDoble(Visitante visitante) throws InterruptedException {
        if (!abierto) {
            return null;
        }

        GomonDoble miGomon = esperandoDobles.poll();
        if (miGomon != null) {
            if (abierto) {
                miGomon.subirAlGomon(visitante);
            } else {
                return null;
            }
        } else {
            // SI NO HAY NADIE ESPERANDO COMPAÑERO
            miGomon = disponiblesDobles.poll(8, TimeUnit.SECONDS);
            if (miGomon == null || !abierto) {
                return null;
            } else {
                miGomon.subirAlGomon(visitante);
                esperandoDobles.put(miGomon);
                // Espera con tiempo límite y verificación periódica de 'abierto'
                miGomon.esperarCompañero(abierto);
                if (!abierto) {
                    // Si el parque cerró durante la espera, liberamos el gomon
                    disponiblesDobles.add(miGomon);
                    return null;
                }
            }
        }
        return miGomon;
    }

    public GomonIndividual subirIndividual(Visitante visitante) throws InterruptedException {
        GomonIndividual miGomon;
        miGomon = disponiblesIndividuales.poll(8000, TimeUnit.MILLISECONDS);
        if (miGomon == null || !abierto) {
            //System.out.println("GOMONES --- " + Thread.currentThread().getName()+ " se retira ya que no hay gomones individuales.");
            return null;
        }
        miGomon.añadirVisitante(visitante);
        return miGomon;
    }

    public Gomon subirGomon(boolean usaGomonDoble, Visitante visitante) throws InterruptedException {
        Gomon miGomon;
        if (!abierto) {
            return null;
        }
        if (usaGomonDoble) {
            miGomon = subirDoble(visitante);
        } else {
            miGomon = subirIndividual(visitante);
        }
        return miGomon;
    }

    public void realizarActividad(Gomon miGomon, boolean usaGomonDoble, Visitante visitante)
            throws InterruptedException {
        if (miGomon != null) {
            if (miGomon.esPrimerVisitante(visitante)) {
                try {
                    lockCarrera.lock();
                    while (enCarrera) {
                        esperandoCarrera.await();
                        if (!abierto) {
                            return;
                        }
                    }
                    gomonesListos++;
                    if (gomonesListos == H) {
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
                    //System.out.println("AVISO A MI COMPAAAAAA");
                    ((GomonDoble) miGomon).avisar();
                } else {
                    devolverGomon(miGomon, false);
                }
            } else {
                esperandoCompañero.add((GomonDoble) miGomon);
                ((GomonDoble) miGomon).esperarCompañero(abierto);
                if (abierto) {
                    devolverGomon(miGomon, true);
                }
            }
        }
    }

    public void devolverGomon(Gomon miGomon, boolean usaGomonDoble) throws InterruptedException {
        if (miGomon != null) {
            if (usaGomonDoble) {
                ((GomonDoble) miGomon).reiniciar();
                lockGomonDoble.lock();
                disponiblesDobles.put((GomonDoble) miGomon);
                lockGomonDoble.unlock();
            } else {
                ((GomonIndividual) miGomon).reiniciar();
                lockGomonIndividual.lock();
                disponiblesIndividuales.put((GomonIndividual) miGomon);
                lockGomonIndividual.unlock();
            }
        }
    }
}