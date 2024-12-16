package Recursos;

import java.util.concurrent.Semaphore;

public class Tienda {
    private Semaphore cajas = new Semaphore(2);
    private Semaphore clientesEsperando = new Semaphore(0);

    public void comprar() throws InterruptedException {
        cajas.acquire();
        clientesEsperando.release();
    }

    public void atender() throws InterruptedException {
        clientesEsperando.acquire();
        cajas.release();
    }
}
