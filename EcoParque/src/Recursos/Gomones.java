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
    private final HashMap<Visitante, Integer> bolsos = new HashMap<>();

    // Donde estan los gomones
    private BlockingQueue<GomonIndividual> disponiblesIndividuales = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> disponiblesDobles = new LinkedBlockingQueue<>();
    private BlockingQueue<GomonDoble> esperandoDobles = new LinkedBlockingQueue<>();

    private int gomonesListos = 0;
    private int bolsoActual = 0;
    private int posiciones = 0;
    private boolean enCarrera = false;

    public Gomones() {
        for (int i = 0; i < 5; i++) {
            disponiblesDobles.add(new GomonDoble(i + 1));
        }
        for (int i = 5; i < 10; i++) {
            disponiblesIndividuales.add(new GomonIndividual(i + 1));
        }
    }

    public void realizarActividad(boolean usaGomonDoble, Visitante visitante)
            throws InterruptedException, BrokenBarrierException {
        // Obtener un gomon
        lockCarrera.lock();
        Gomon miGomon;
        if (usaGomonDoble) {
            if (!esperandoDobles.isEmpty()) {
                miGomon = esperandoDobles.take();
                ((GomonDoble) miGomon).añadirCompañero(visitante);
                while (enCarrera) {
                    esperandoCarrera.await();
                }
                gomonesListos++;
                System.out.println("GOMONES --- " + Thread.currentThread().getName() + " encontró compañero.");
            } else {
                miGomon = disponiblesDobles.take();
                miGomon.añadirVisitante(visitante);
                esperandoDobles.add((GomonDoble) miGomon);
                System.out.println("GOMONES --- " + Thread.currentThread().getName() + " espera por un compañero.");
            }
            System.out.println("GOMONES --- " + Thread.currentThread().getName() + " obtiene un gomon doble.");
        } else {
            miGomon = disponiblesIndividuales.take();
            miGomon.añadirVisitante(visitante);
            while (enCarrera) {
                esperandoCarrera.await();
            }
            gomonesListos++;
            System.out.println("GOMONES --- " + Thread.currentThread().getName() + " obtiene un gomon individual.");
        }

        if (gomonesListos == H) {
            System.out.println(
                    "GOMONES --- " + Thread.currentThread().getName()
                            + " está listo para la carrera. GOMONES LISTOS PARA EMPEZAR");
            enCarrera = true;
            gomonesListos = 0;
            inicioCarrera.signalAll();
        } else {
            System.out.println("GOMONES --- " + Thread.currentThread().getName()
                    + " está listo para la carrera. Esperando gomones");
            inicioCarrera.await();
        }
        lockCarrera.unlock();
        System.out.println("GOMONES --- " + Thread.currentThread().getName() + " inicia la carrera.");

        Thread.sleep(miGomon.correr());
        switch (posiciones) {
            case 0:
                System.out.println("GOMONES --- El gomón " + miGomon.toString() + " salió primero en la carrera!");
                posiciones++;
                break;
            case 1:
                System.out.println("GOMONES --- El gomón " + miGomon.toString() + " salió segundo en la carrera!");
                posiciones++;
                break;
            case 2:
                System.out.println("GOMONES --- El gomón " + miGomon.toString() + " salió tercero en la carrera!");
                posiciones++;
                break;
            default:
                System.out.println("GOMONES --- El gomón " + miGomon.toString() + " terminó la carrera.");
                break;
        }
    }

    public void elegirTransporte(boolean eleccionTransporte, Visitante visitante) throws InterruptedException {
        // Traslado al inicio
        if (eleccionTransporte) {
            bicicletas.acquire();
            System.out.println("GOMONES --- " + Thread.currentThread().getName() + " usa una bicicleta para llegar.");
            Thread.sleep(1000); // Simula el traslado
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
                "GOMONES --- " + Thread.currentThread().getName() + " termina la carrera y recoge su bolso "
                        + bolsos.get(visitante));
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
                    "GOMONES --- " + Thread.currentThread().getName() + " abordó el tren. Pasajeros actuales: "
                            + pasajerosActuales);

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
            bajarTren.await();
            lockTren.lock();
            pasajerosActuales--;
            System.out.println("GOMONES --- " + Thread.currentThread().getName()
                    + "se baja del tren. Pasajeros actuales: " + pasajerosActuales);
            if (pasajerosActuales == 0) {
                volverTren.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
    }

    public void controlTren() {
        while (true) {
            lockTren.lock();
            try {
                System.out.println("Conductor está esperando completar o tiempo límite...");
                trenListo.await(TIEMPO_ESPERA, TimeUnit.SECONDS); // Esperar hasta llenar el tren o alcanzar el tiempo

                // Salida del tren
                if (pasajerosActuales > 0) {
                    System.out.println("El tren sale con " + pasajerosActuales + " pasajeros.");
                    trenEnViaje = true;

                    // Simula el viaje
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lockTren.unlock();
            }
        }
    }
}
