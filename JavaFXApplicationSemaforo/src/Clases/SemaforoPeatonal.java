package Clases;

import java.util.concurrent.Semaphore;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

public class SemaforoPeatonal {
    private Semaphore semaforo;
    private int peatonPosX = 500;
    private int peatonPosY = 400;
    private Image peatonImagen;

    public SemaforoPeatonal() {
        this.semaforo = new Semaphore(1);
        peatonImagen = new Image(getClass().getResource("/src/resources/peaton2.jpg").toExternalForm());

    }

    public void moverPeatones(GraphicsContext gc, Label peatonSemaforoLabel) {
        try {
            // Los peatones esperan hasta que el semáforo esté en verde
            semaforo.acquire(); // Adquirir el semáforo para permitir el cruce

            // Dibujar la imagen del peatón en lugar de un rectángulo
            gc.drawImage(peatonImagen, peatonPosX, peatonPosY, 30, 30); // Ajusta el tamaño según la imagen

            peatonPosY -= 5; // Mover peatón hacia adelante
            if (peatonPosY < 0) {
                peatonPosY = 400; // Reiniciar posición del peatón cuando sale de la pantalla
            }

            peatonSemaforoLabel.setText("Semáforo Peatonal: Verde");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Libera el semáforo para permitir que otro peatón cruce
            semaforo.release();
        }
    }
}
