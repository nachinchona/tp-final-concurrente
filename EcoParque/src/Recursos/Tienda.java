package Recursos;

import java.util.concurrent.Semaphore;

import Hilos.Visitante;

public class Tienda {
    private Semaphore cajas = new Semaphore(2);
    private Semaphore clientesEsperando = new Semaphore(0);
    private Semaphore esperarFactura = new Semaphore(0);
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_CYAN = "\u001B[46m";

    public void comprar(Visitante visitante) throws InterruptedException {
        Thread.sleep(1000);
        cajas.acquire();
        System.out.println(ANSI_RED + "TIENDA --- " + Thread.currentThread().getName() + " compra souvenir." + ANSI_RESET);
        clientesEsperando.release();
        esperarFactura.acquire();
        System.out.println(ANSI_GREEN + "TIENDA --- " + Thread.currentThread().getName() + " se va de la tienda." + ANSI_RESET);
        cajas.release();
    }

    public void atender() throws InterruptedException {
        clientesEsperando.acquire();
        Thread.sleep(1000);
        esperarFactura.release();
    }
}
