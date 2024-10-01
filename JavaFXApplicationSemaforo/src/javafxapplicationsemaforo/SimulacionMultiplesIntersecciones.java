import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class SimulacionMultiplesIntersecciones extends JFrame {
    private static Semaphore semaforo = new Semaphore(0); // Semáforo compartido entre intersecciones
    private static boolean luzVerde = false; // Estado compartido del semáforo (rojo/verde)
    private ArrayList<Vehiculo> vehiculos = new ArrayList<>(); // Lista dinámica de vehículos
    private Interseccion[] intersecciones; // Arreglo de intersecciones
    private JPanel panelCarretera; // Panel donde los vehículos se moverán
    private int tiempoVerde = 5000; // Tiempo en verde
    private int tiempoRojo = 5000; // Tiempo en rojo

    public SimulacionMultiplesIntersecciones() {
        setTitle("Simulación de Múltiples Intersecciones");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crear dos intersecciones sincronizadas
        intersecciones = new Interseccion[2];
        intersecciones[0] = new Interseccion(50, 50); // Primera intersección
        intersecciones[1] = new Interseccion(400, 50); // Segunda intersección

        // Panel de la carretera donde se mueven los vehículos
        panelCarretera = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Dibujar intersecciones
                for (Interseccion interseccion : intersecciones) {
                    interseccion.dibujarSemaforo(g);
                }

                // Dibujar vehículos
                for (Vehiculo vehiculo : vehiculos) {
                    g.setColor(vehiculo.color);
                    g.fillRect(vehiculo.posX, vehiculo.posY, 40, 20); // Dibujar el vehículo
                }
            }
        };
        panelCarretera.setBackground(Color.GRAY);
        add(panelCarretera, BorderLayout.CENTER);

        // Crear panel de control para agregar vehículos
        JPanel panelControl = new JPanel();
        JButton btnAgregarVehiculo = new JButton("Agregar Vehículo");

        panelControl.add(btnAgregarVehiculo);
        add(panelControl, BorderLayout.SOUTH);

        // Acción del botón para agregar un vehículo
        btnAgregarVehiculo.addActionListener(e -> {
            agregarVehiculo();
            System.out.println("Vehículo agregado. Total de vehículos: " + vehiculos.size());
        });

        // Iniciar el control del semáforo y los vehículos
        new Thread(new ControlSemaforo()).start();
        new Thread(new MovimientoVehiculos()).start();
    }

    // Método para agregar un nuevo vehículo
    private void agregarVehiculo() {
        Vehiculo nuevoVehiculo = new Vehiculo(0, 100, getRandomColor());
        vehiculos.add(nuevoVehiculo);
    }

    // Método para generar colores aleatorios para los vehículos
    private Color getRandomColor() {
        return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    // Clase para representar una intersección con su semáforo
    class Interseccion {
        int posX, posY;
        Color colorSemaforo;

        public Interseccion(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            this.colorSemaforo = Color.RED; // Inicialmente en rojo
        }

        public void cambiarEstadoSemaforo(boolean luzVerde) {
            this.colorSemaforo = luzVerde ? Color.GREEN : Color.RED;
        }

        public void dibujarSemaforo(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(posX, posY, 50, 150); // Poste del semáforo
            g.setColor(colorSemaforo);
            g.fillOval(posX + 10, posY + 50, 30, 30); // Luz del semáforo
        }
    }

    // Clase para representar un vehículo
    class Vehiculo {
        int posX, posY;
        Color color;

        public Vehiculo(int posX, int posY, Color color) {
            this.posX = posX;
            this.posY = posY;
            this.color = color;
        }

        public void mover() {
            if (luzVerde) {
                posX += 5; // Mueve el vehículo cuando la luz está verde
                if (posX > getWidth()) {
                    posX = -40; // Regresa el vehículo al inicio si sale de la pantalla
                }
            }
        }
    }

    // Clase que controla los semáforos
    class ControlSemaforo implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // Tiempo en rojo
                    Thread.sleep(tiempoRojo);
                    luzVerde = true;

                    // Cambiar los semáforos de todas las intersecciones
                    for (Interseccion interseccion : intersecciones) {
                        interseccion.cambiarEstadoSemaforo(true); // Luz verde
                    }
                    repaint();
                    System.out.println("\nSemáforo verde: Los vehículos pueden pasar.");

                    // Tiempo en verde
                    Thread.sleep(tiempoVerde);
                    luzVerde = false;

                    // Cambiar los semáforos de todas las intersecciones
                    for (Interseccion interseccion : intersecciones) {
                        interseccion.cambiarEstadoSemaforo(false); // Luz roja
                    }
                    repaint();
                    System.out.println("\nSemáforo rojo: Los vehículos deben detenerse.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Clase que controla el movimiento de los vehículos
    class MovimientoVehiculos implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    for (Vehiculo vehiculo : vehiculos) {
                        vehiculo.mover(); // Mueve el vehículo si el semáforo está en verde
                    }
                    panelCarretera.repaint(); // Redibuja la carretera
                    Thread.sleep(100); // Controla la velocidad de movimiento
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Método principal para ejecutar la simulación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimulacionMultiplesIntersecciones().setVisible(true);
        });
    }
}
