package Recursos;

import java.awt.Font;
import javax.swing.*;

public class Interfaz {
    private int hora = 9;
    private int minuto = 0;
    private JFrame frame = new JFrame("EcoParque 2024");
    private JPanel panelPrincipal = new JPanel();
    private JLabel titulo = new JLabel("Panel de control EcoParque");
    private JLabel horario = new JLabel(hora + ":" + minuto);
    private int cantEnColectivo = 0;
    private int cantVisitantesActuales = 0;
    private int cantVisitantesTotales = 0;
    private JLabel colectivo = new JLabel("Pasajeros en colectivo folklórico: 0");
    private JLabel visitantesActuales = new JLabel("Visitantes actuales: 0");
    private JLabel visitantesTotales = new JLabel("Visitantes totales: 0");
    private JLabel viajes = new JLabel("Viajes totales: 0");

    public Interfaz() {
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        horario.setFont(new Font("Consolas", Font.PLAIN, 25));
        panelPrincipal.add(titulo);
        panelPrincipal.add(horario);
        titulo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        horario.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panelPrincipal.add(colectivo);
        panelPrincipal.add(visitantesActuales);
        panelPrincipal.add(visitantesTotales);
        panelPrincipal.add(viajes);
        colectivo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        visitantesActuales.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        visitantesTotales.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        viajes.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        frame.add(panelPrincipal);
        frame.setSize(330, 170);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void iniciarInterfaz() {
        frame.setVisible(true);
    }

    public void reiniciarInterfaz() {
        colectivo.setText("Pasajeros en colectivo folklórico: 0");
        visitantesActuales.setText("Visitantes actuales: 0");
        visitantesTotales.setText("Visitantes totales: 0");
        viajes.setText("Viajes totales: 0");
    }

    public int getHora() {
        return hora;
    }

    public int getMinuto() {
        return minuto;
    }

    public void aumentarTiempo() {
        minuto++;
        if (minuto == 60) {
            minuto = 0;
            hora++;
        }
        if (minuto / 10 == 0) {
            horario.setText(hora + ":0" + minuto);
        } else {
            horario.setText(hora + ":" + minuto);
        }
    }

    public synchronized void aumentarPasajeros() {
        cantEnColectivo++;
        colectivo.setText("Pasajeros en colectivo folklórico: " + Integer.toString(cantEnColectivo));
    }

    public synchronized void decrementarPasajeros() {
        cantEnColectivo--;
        colectivo.setText("Pasajeros en colectivo folklórico: " + Integer.toString(cantEnColectivo));
    }

    public synchronized void aumentarVisitantes() {
        cantVisitantesActuales++;
        cantVisitantesTotales++;
        visitantesActuales.setText("Visitantes actuales: " + Integer.toString(cantVisitantesActuales));
        visitantesTotales.setText("Visitantes totales: " + Integer.toString(cantVisitantesTotales));
    }

    public synchronized void decrementarVisitantes() {
        cantVisitantesActuales--;
        visitantesActuales.setText("Visitantes actuales: " + Integer.toString(cantVisitantesActuales));
    }

    public synchronized void setViajes(int cantViajes) {
        viajes.setText("Viajes totales: " + Integer.toString(cantViajes));
    }
}
