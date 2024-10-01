package Clases;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.util.concurrent.Semaphore;

public class SemaforoVehicularVertical extends Thread {
    private int posX = 290;  // Posición fija en X (centrado en la calle vertical)
    private int posY = 0;  // Comienza en la parte superior de la pantalla
    private GraphicsContext gc;
    private Semaphore semaforoInterseccion;  // Semáforo para controlar el cruce de la intersección
    private Label hilosActivosLabel;  // Etiqueta para actualizar el número de hilos activos
    private boolean[] semaforoVerticalVerde;  // Estado del semáforo vertical como array compartido
    private int[] contadorHilos;  // Referencia al contador de hilos activos

    public SemaforoVehicularVertical(GraphicsContext gc, Semaphore semaforoInterseccion, Label hilosActivosLabel, boolean[] semaforoVerticalVerde, int[] contadorHilos) {
        this.gc = gc;
        this.semaforoInterseccion = semaforoInterseccion;
        this.hilosActivosLabel = hilosActivosLabel;
        this.semaforoVerticalVerde = semaforoVerticalVerde;  // Acceso compartido al estado del semáforo vertical
        this.contadorHilos = contadorHilos;  // Referencia al contador de hilos activos
    }

    @Override
    public void run() {
        try {
            while (posY < 600) {
                // Limpiar la posición anterior
                gc.clearRect(posX, posY - 5, 35, 45);

                // Dibujar el vehículo (rectángulo rojo)
                gc.setFill(Color.RED);
                gc.fillRect(posX, posY, 30, 40);

                // Verificar si el vehículo está cerca de la intersección (antes de los 250px)
                if (posY >= 230 && posY <= 250) {
                    // Pausar el vehículo en la cola si el semáforo está en rojo o la intersección está ocupada
                    while (!semaforoVerticalVerde[0] || semaforoInterseccion.availablePermits() == 0) {
                        Thread.sleep(100);  // Pausar mientras espera su turno
                    }

                    // Adquirir el semáforo para cruzar la intersección si el semáforo está en verde
                    semaforoInterseccion.acquire();
                }

                // Mover el vehículo
                posY += 5;

                // Si ya cruzó la intersección, liberar el semáforo
                if (posY > 350) {
                    semaforoInterseccion.release();
                }

                // Pausa para simular el movimiento
                Thread.sleep(100);
            }

            // Decrementar el contador de hilos activos al terminar
            synchronized (contadorHilos) {
                contadorHilos[0]--;  // Restar del contador de hilos activos
                hilosActivosLabel.setText("Hilos activos: " + contadorHilos[0]);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
