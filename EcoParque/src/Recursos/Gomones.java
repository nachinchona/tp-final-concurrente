package Recursos;

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
    private int pasajerosActuales = 0; // Contador de pasajeros en el tren
    private boolean trenEnViaje = false; // Indica si el tren está en viaje

    private final Semaphore bicicletas = new Semaphore(CANTIDAD_BICICLETAS, true);
    private final Semaphore gomonesIndividuales = new Semaphore(CANTIDAD_GOMONES_INDIVIDUALES, true);
    private final Semaphore gomonesDobles = new Semaphore(CANTIDAD_GOMONES_DOBLES, true);
    private final CyclicBarrier inicioCarrera = new CyclicBarrier(H);
    private final BlockingQueue<Integer> bolsos = new ArrayBlockingQueue<>(50);

    public void realizarActividad(String metodoTransporte, boolean usaGomonDoble) throws InterruptedException, BrokenBarrierException {
        // Traslado al inicio
        if (metodoTransporte.equals("bicicleta")) {
            bicicletas.acquire();
            System.out.println(Thread.currentThread().getName() + " usa una bicicleta para llegar.");
            Thread.sleep(1000); // Simula el traslado
        } else if (metodoTransporte.equals("tren")) {
            usarTren();
        }

        // Guardar pertenencias
        int numeroBolso = Thread.currentThread().hashCode();
        bolsos.put(numeroBolso);
        System.out.println(Thread.currentThread().getName() + " guarda pertenencias en el bolso " + numeroBolso);

        // Obtener un gomon
        if (usaGomonDoble) {
            gomonesDobles.acquire();
            System.out.println(Thread.currentThread().getName() + " obtiene un gomon doble.");
        } else {
            gomonesIndividuales.acquire();
            System.out.println(Thread.currentThread().getName() + " obtiene un gomon individual.");
        }

        // Esperar para iniciar la carrera
        System.out.println(Thread.currentThread().getName() + " está listo para la carrera.");
        inicioCarrera.await();

        System.out.println(Thread.currentThread().getName() + " inicia la carrera.");

        // Liberar recursos al final
        if (usaGomonDoble) {
            gomonesDobles.release();
        } else {
            gomonesIndividuales.release();
        }
        System.out.println(Thread.currentThread().getName() + " termina la carrera y recoge su bolso " + numeroBolso);
        bolsos.remove(numeroBolso);
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
            System.out.println(Thread.currentThread().getName() + " abordó el tren. Pasajeros actuales: " + pasajerosActuales);

            // Si se llena el tren, notificar al conductor
            if (pasajerosActuales == CAPACIDAD_TREN) {
                trenListo.signalAll(); // Despertar al conductor para que salga
            }
        } finally {
            lockTren.unlock();
        }
    }

    private void controlTren() {
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

