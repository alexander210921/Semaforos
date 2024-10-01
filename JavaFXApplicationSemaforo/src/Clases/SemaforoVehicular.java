package Clases;

import java.util.concurrent.Semaphore;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

public class SemaforoVehicular {
    private Semaphore semaforo;
    private int vehiculoPosX = 100;
    private int vehiculoPosY = 250;
    private static final int WIDTH = 600;
    private Image vehiculoImagen;

    public SemaforoVehicular() {
        // Inicializamos el semáforo con 1 permiso
        this.semaforo = new Semaphore(1);

        // Cargar la imagen del vehículo
        vehiculoImagen = new Image(getClass().getResource("/src/resources/vehiculo.jpg").toExternalForm());

    }

    public void moverVehiculos(GraphicsContext gc, Label vehiculoSemaforoLabel) {
        try {
            // Los vehículos esperan hasta que el semáforo esté en verde
            semaforo.acquire(); // Adquirir el semáforo para permitir el cruce

            // Dibujar la imagen del vehículo en lugar de un rectángulo
            gc.drawImage(vehiculoImagen, vehiculoPosX, vehiculoPosY, 60, 40); // Ajusta el tamaño según la imagen

            vehiculoPosX += 5; // Mover vehículo hacia adelante
            if (vehiculoPosX > WIDTH) {
                vehiculoPosX = 100; // Reiniciar posición del vehículo cuando sale de la pantalla
            }

            vehiculoSemaforoLabel.setText("Semáforo Vehicular: Verde");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Libera el semáforo para permitir que otro vehículo cruce
            semaforo.release();
        }
    }
}
