import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class SimulacionSemaforoGrafico extends JFrame {
    private static Semaphore semaforo = new Semaphore(0); // Inicialmente está en rojo
    private static boolean luzVerde = false; // Estado del semáforo (rojo/verde)
    private ArrayList<Vehiculo> vehiculos = new ArrayList<>(); // Lista dinámica de vehículos
    private JPanel panelSemaforo; // Panel donde se muestra el semáforo
    private JPanel panelCarretera; // Panel donde los vehículos se moverán
    private int tiempoVerde = 5000; // Tiempo en verde
    private int tiempoRojo = 5000; // Tiempo en rojo

    public SimulacionSemaforoGrafico() {
        setTitle("Simulación Semáforo con Vehículos");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel del semáforo
        panelSemaforo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(50, 50, 50, 150); // Dibujar poste del semáforo
                
                // Dibujar luces del semáforo
                if (luzVerde) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.RED);
                }
                g.fillOval(60, 60, 30, 30); // Luz roja/verde
            }
        };
        panelSemaforo.setPreferredSize(new Dimension(150, 400));
        add(panelSemaforo, BorderLayout.WEST);

        // Panel de la carretera donde se mueven los vehículos
        panelCarretera = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (Vehiculo vehiculo : vehiculos) {
                    g.setColor(vehiculo.color);
                    g.fillRect(vehiculo.posX, vehiculo.posY, 40, 20); // Dibujar el vehículo
                }
            }
        };
        panelCarretera.setBackground(Color.GRAY);
        add(panelCarretera, BorderLayout.CENTER);

        // Crear panel de control de semáforo y vehículos
        JPanel panelControl = new JPanel();
        panelControl.setLayout(new GridLayout(3, 2));

        JLabel labelVerde = new JLabel("Tiempo en verde (ms): ");
        JTextField txtVerde = new JTextField(String.valueOf(tiempoVerde));
        JLabel labelRojo = new JLabel("Tiempo en rojo (ms): ");
        JTextField txtRojo = new JTextField(String.valueOf(tiempoRojo));
        JButton btnAplicar = new JButton("Aplicar Cambios");

        // Botones para agregar/eliminar vehículos
        JButton btnAgregarVehiculo = new JButton("Agregar Vehículo");
        JButton btnEliminarVehiculo = new JButton("Eliminar Vehículo");

        panelControl.add(labelVerde);
        panelControl.add(txtVerde);
        panelControl.add(labelRojo);
        panelControl.add(txtRojo);
        panelControl.add(btnAplicar);
        panelControl.add(btnAgregarVehiculo);
        panelControl.add(btnEliminarVehiculo);

        add(panelControl, BorderLayout.SOUTH);

        // Acción del botón para aplicar cambios en los tiempos
        btnAplicar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tiempoVerde = Integer.parseInt(txtVerde.getText());
                tiempoRojo = Integer.parseInt(txtRojo.getText());
                System.out.println("Nuevos tiempos - Verde: " + tiempoVerde + "ms, Rojo: " + tiempoRojo + "ms");
            }
        });

        // Acción del botón para agregar un vehículo
        btnAgregarVehiculo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarVehiculo();
                System.out.println("Vehículo agregado. Total de vehículos: " + vehiculos.size());
            }
        });

        // Acción del botón para eliminar un vehículo
        btnEliminarVehiculo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarVehiculo();
                System.out.println("Vehículo eliminado. Total de vehículos: " + vehiculos.size());
            }
        });

        // Iniciar el control del semáforo y los vehículos
        new Thread(new ControlSemaforo()).start();
        new Thread(new MovimientoVehiculos()).start();
    }

    // Método para agregar un nuevo vehículo
    private void agregarVehiculo() {
        Vehiculo nuevoVehiculo = new Vehiculo(50, 300, getRandomColor());
        vehiculos.add(nuevoVehiculo);
    }

    // Método para eliminar un vehículo (si hay al menos uno)
    private void eliminarVehiculo() {
        if (!vehiculos.isEmpty()) {
            vehiculos.remove(vehiculos.size() - 1);
        }
    }

    // Método para generar colores aleatorios para los vehículos
    private Color getRandomColor() {
        return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
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

    // Clase que controla el semáforo
    class ControlSemaforo implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // Tiempo en rojo
                    Thread.sleep(tiempoRojo);
                    luzVerde = true;
                    semaforo.release(vehiculos.size()); // Permitir el paso de los vehículos
                    repaint();
                    System.out.println("\nSemáforo verde: Los vehículos pueden pasar.");

                    // Tiempo en verde
                    Thread.sleep(tiempoVerde);
                    luzVerde = false;
                    semaforo = new Semaphore(0); // Bloquear el paso
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
            new SimulacionSemaforoGrafico().setVisible(true);
        });
    }
}
