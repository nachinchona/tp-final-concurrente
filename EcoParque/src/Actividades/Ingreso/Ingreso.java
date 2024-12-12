package Actividades.Ingreso;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Ingreso {
    private CyclicBarrier colectivoBarrier = new CyclicBarrier(25, () -> {System.out.println("Comienza tour por colectivo folklórico!");});
    private Semaphore molinetes = new Semaphore(6);

    public void ingresarPorTour() throws InterruptedException, BrokenBarrierException {
        System.out.println(Thread.currentThread().getName() + " entró a colectivo folklórico.");
        colectivoBarrier.await();
    }

    public void entrarMolinete() throws InterruptedException {
        molinetes.acquire();
        System.out.println(Thread.currentThread().getName() + " entró al molinete.");
    }
}
