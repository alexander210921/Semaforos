import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.Semaphore;

public class CruceSemaforosFX extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private Semaphore semaforoVehicular = new Semaphore(1);
    private Semaphore semaforoPeatonal = new Semaphore(1);
    
    // Posiciones de vehículos y peatones
    private int vehiculoPosX = 100;
    private int vehiculoPosY = 250;
    private int peatonPosX = 500;
    private int peatonPosY = 400;

    @Override
    public void start(Stage primaryStage) {
        // Crear el lienzo donde se dibujarán vehículos y peatones
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Panel que contiene el lienzo
        Pane root = new Pane();
        root.getChildren().add(canvas);

        // Etiquetas para los semáforos
        Label vehiculoSemaforoLabel = new Label("Semáforo Vehicular: Verde");
        vehiculoSemaforoLabel.setLayoutX(20);
        vehiculoSemaforoLabel.setLayoutY(20);

        Label peatonSemaforoLabel = new Label("Semáforo Peatonal: Rojo");
        peatonSemaforoLabel.setLayoutX(20);
        peatonSemaforoLabel.setLayoutY(50);

        root.getChildren().addAll(vehiculoSemaforoLabel, peatonSemaforoLabel);

        // Crear una escena
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Simulador de Cruce de Vehículos y Peatones");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Iniciar el proceso de animación
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            gc.clearRect(0, 0, WIDTH, HEIGHT); // Limpiar el lienzo
            dibujarInterseccion(gc); // Dibujar la intersección
            moverVehiculos(gc, vehiculoSemaforoLabel);
            moverPeatones(gc, peatonSemaforoLabel);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // Animación infinita
        timeline.play();
    }

    private void dibujarInterseccion(GraphicsContext gc) {
        gc.setFill(Color.GRAY);
        gc.fillRect(200, 200, 200, 200); // Representa el cruce de las calles
    }

    private void moverVehiculos(GraphicsContext gc, Label semaforoVehicularLabel) {
        try {
            // Simulación de semáforo vehicular verde y paso de vehículos
            semaforoVehicular.acquire(); // El vehículo puede cruzar
            gc.setFill(Color.BLUE);
            gc.fillRect(vehiculoPosX, vehiculoPosY, 40, 20); // Dibuja el vehículo
            vehiculoPosX += 5; // Mover vehículo hacia adelante
            if (vehiculoPosX > WIDTH) {
                vehiculoPosX = 100; // Reiniciar posición del vehículo
            }
            semaforoVehicularLabel.setText("Semáforo Vehicular: Verde");
            Thread.sleep(500); // Simulación de tiempo de cruce
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforoVehicular.release();
        }
    }

    private void moverPeatones(GraphicsContext gc, Label semaforoPeatonalLabel) {
        try {
            // Simulación de semáforo peatonal verde y paso de peatones
            semaforoPeatonal.acquire(); // El peatón puede cruzar
            gc.setFill(Color.RED);
            gc.fillRect(peatonPosX, peatonPosY, 20, 20); // Dibuja el peatón
            peatonPosY -= 5; // Mover peatón hacia adelante
            if (peatonPosY < 0) {
                peatonPosY = 400; // Reiniciar posición del peatón
            }
            semaforoPeatonalLabel.setText("Semáforo Peatonal: Verde");
            Thread.sleep(500); // Simulación de tiempo de cruce
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforoPeatonal.release();
        }
    }
}
