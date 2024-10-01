package javafxapplicationsemaforo;

import Clases.SemaforoVehicularHorizontal;
import Clases.SemaforoVehicularVertical;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.concurrent.Semaphore;

public class JavaFXApplicationSemaforo extends Application {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;

    // Semáforo para controlar el acceso a la intersección
    private final Semaphore semaforoInterseccion = new Semaphore(1);  // Solo permite un vehículo en la intersección a la vez

    // Variables para controlar el estado de los semáforos (como arrays compartidos)
    private boolean[] semaforoHorizontalVerde = {true};  // Semáforo horizontal compartido
    private boolean[] semaforoVerticalVerde = {false};  // Semáforo vertical compartido

    // Contador de hilos activos
    private int[] contadorHilos = {0};  // Usamos un arreglo para que los hilos puedan modificar su valor

    @Override
    public void start(Stage primaryStage) {
        // Crear el lienzo donde se dibujarán los vehículos y las calles
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Panel que contiene el lienzo
        Pane root = new Pane();
        root.getChildren().add(canvas);

        // Etiquetas para mostrar el estado de los semáforos
        Label estadoSemaforoHorizontal = new Label("Semáforo Vehicular Horizontal: Verde");
        estadoSemaforoHorizontal.setLayoutX(20);
        estadoSemaforoHorizontal.setLayoutY(20);

        Label estadoSemaforoVertical = new Label("Semáforo Vehicular Vertical: Rojo");
        estadoSemaforoVertical.setLayoutX(20);
        estadoSemaforoVertical.setLayoutY(50);

        // Etiqueta para mostrar el número de hilos activos
        Label hilosActivosLabel = new Label("Hilos activos: 0");
        hilosActivosLabel.setLayoutX(20);
        hilosActivosLabel.setLayoutY(80);

        // Crear botones para agregar vehículos
        Button agregarVehiculoHorizontal = new Button("Añadir Vehículo Horizontal");
        agregarVehiculoHorizontal.setLayoutX(20);
        agregarVehiculoHorizontal.setLayoutY(120);
        agregarVehiculoHorizontal.setOnAction(e -> {
            // Crear un nuevo vehículo horizontal y lanzarlo en un hilo
            SemaforoVehicularHorizontal vehiculo = new SemaforoVehicularHorizontal(gc, semaforoInterseccion, hilosActivosLabel, semaforoHorizontalVerde, contadorHilos);
            vehiculo.start();  // Iniciar el hilo

            // Incrementar el contador de hilos activos
            synchronized (contadorHilos) {
                contadorHilos[0]++;
                hilosActivosLabel.setText("Hilos activos: " + contadorHilos[0]);
            }
        });

        Button agregarVehiculoVertical = new Button("Añadir Vehículo Vertical");
        agregarVehiculoVertical.setLayoutX(20);
        agregarVehiculoVertical.setLayoutY(160);
        agregarVehiculoVertical.setOnAction(e -> {
            // Crear un nuevo vehículo vertical y lanzarlo en un hilo
            SemaforoVehicularVertical vehiculo = new SemaforoVehicularVertical(gc, semaforoInterseccion, hilosActivosLabel, semaforoVerticalVerde, contadorHilos);
            vehiculo.start();  // Iniciar el hilo

            // Incrementar el contador de hilos activos
            synchronized (contadorHilos) {
                contadorHilos[0]++;
                hilosActivosLabel.setText("Hilos activos: " + contadorHilos[0]);
            }
        });

        // Añadir los botones y etiquetas al panel
        root.getChildren().addAll(estadoSemaforoHorizontal, estadoSemaforoVertical, hilosActivosLabel, agregarVehiculoHorizontal, agregarVehiculoVertical);

        // Crear una escena con el panel principal
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Simulador de Cruce con Semáforos de Programación");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Timeline para cambiar el estado de los semáforos y actualizar las etiquetas
        Timeline semaforoTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            // Alternar los semáforos entre horizontal y vertical
            semaforoHorizontalVerde[0] = !semaforoHorizontalVerde[0];
            semaforoVerticalVerde[0] = !semaforoVerticalVerde[0];

            // Actualizar el estado del semáforo horizontal
            if (semaforoHorizontalVerde[0]) {
                estadoSemaforoHorizontal.setText("Semáforo Vehicular Horizontal: Verde");
            } else {
                estadoSemaforoHorizontal.setText("Semáforo Vehicular Horizontal: Rojo");
            }

            // Actualizar el estado del semáforo vertical
            if (semaforoVerticalVerde[0]) {
                estadoSemaforoVertical.setText("Semáforo Vehicular Vertical: Verde");
            } else {
                estadoSemaforoVertical.setText("Semáforo Vehicular Vertical: Rojo");
            }
        }));
        semaforoTimeline.setCycleCount(Timeline.INDEFINITE);
        semaforoTimeline.play();

        // Timeline para animar la interfaz y dibujar las calles
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            // Limpiar el lienzo
            gc.clearRect(0, 0, WIDTH, HEIGHT);

            // Dibujar la intersección en forma de cruz
            dibujarInterseccion(gc);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // Método para dibujar la calle en forma de cruz
    private void dibujarInterseccion(GraphicsContext gc) {
        gc.setFill(Color.DARKGRAY);

        // Dibujar la calle vertical (cruce Norte-Sur)
        gc.fillRect(275, 0, 50, HEIGHT);  // Rectángulo largo y delgado, centrado verticalmente

        // Dibujar la calle horizontal (cruce Este-Oeste)
        gc.fillRect(0, 275, WIDTH, 50);  // Rectángulo largo y delgado, centrado horizontalmente
    }

    // Método principal para lanzar la aplicación JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
