package Clases;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.util.concurrent.Semaphore;

public class SemaforoVehicularHorizontal extends Thread {
    private int posX = 0;  // Comienza a la izquierda de la pantalla
    private int posY = 290;  // Posición fija en Y (centrado en la calle horizontal)
    private GraphicsContext gc;
    private Semaphore semaforoInterseccion;  // Semáforo para controlar el cruce de la intersección
    private Label hilosActivosLabel;  // Etiqueta para actualizar el número de hilos activos
    private boolean[] semaforoHorizontalVerde;  // Estado del semáforo horizontal como array compartido
    private int[] contadorHilos;  // Referencia al contador de hilos activos

    public SemaforoVehicularHorizontal(GraphicsContext gc, Semaphore semaforoInterseccion, Label hilosActivosLabel, boolean[] semaforoHorizontalVerde, int[] contadorHilos) {
        this.gc = gc;
        this.semaforoInterseccion = semaforoInterseccion;
        this.hilosActivosLabel = hilosActivosLabel;
        this.semaforoHorizontalVerde = semaforoHorizontalVerde;  // Acceso compartido al estado del semáforo
        this.contadorHilos = contadorHilos;  // Referencia al contador de hilos activos
    }

    @Override
    public void run() {
        try {
            while (posX < 600) {
                // Limpiar la posición anterior
                gc.clearRect(posX - 5, posY, 45, 35);

                // Dibujar el vehículo (rectángulo azul)
                gc.setFill(Color.BLUE);
                gc.fillRect(posX, posY, 40, 30);

                // Verificar si el vehículo está cerca de la intersección (antes de los 250px)
                if (posX >= 230 && posX <= 250) {
                    // Pausar el vehículo en la cola si el semáforo está en rojo o la intersección está ocupada
                    while (!semaforoHorizontalVerde[0] || semaforoInterseccion.availablePermits() == 0) {
                        Thread.sleep(100);  // Pausar mientras espera su turno
                    }

                    // Adquirir el semáforo para cruzar la intersección si el semáforo está en verde
                    semaforoInterseccion.acquire();
                }

                // Mover el vehículo
                posX += 5;

                // Si ya cruzó la intersección, liberar el semáforo
                if (posX > 350) {
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
