package javafxapplicationsemaforo;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class SimulacionInterseccionCruzada extends JFrame {
    private static Semaphore semaforoHorizontal = new Semaphore(0); // Inicialmente bloqueado para vehículos horizontales
    private static Semaphore semaforoVertical = new Semaphore(0);   // Inicialmente bloqueado para vehículos verticales
    private static boolean luzVerdeHorizontal = true; // Estado del semáforo para el flujo horizontal (inicia en verde)
    private ArrayList<Vehiculo> vehiculosHorizontales = new ArrayList<>();
    private ArrayList<Vehiculo> vehiculosVerticales = new ArrayList<>();
    private JPanel panelCarretera; // Panel donde los vehículos se moverán
    private int tiempoMinimoVerde = 3000; // Tiempo mínimo en verde para cada semáforo
    private int tiempoMaximoVerde = 7000; // Tiempo máximo en verde (en base a la cantidad de vehículos)
    private int tiempoRojo = 3000;  // Tiempo en rojo para cada semáforo

    private JSlider sliderTiempoVerdeHorizontal, sliderTiempoVerdeVertical;
    private JLabel lblEstadisticas;

    private int vehiculosPasaronHorizontal = 0;
    private int vehiculosPasaronVertical = 0;
    
    private Timer timerDB;

    public SimulacionInterseccionCruzada() {
        setTitle("Simulación de Intersección Cruzada");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de la carretera donde se mueven los vehículos
        panelCarretera = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                g.setColor(Color.WHITE);
                g.fillRect(350, 250, 100, 100); // Intersección en el centro
                
                g.setColor(Color.GRAY);
                g.fillRect(0, 275, getWidth(), 50); // Carretera horizontal
                g.fillRect(375, 0, 50, getHeight()); // Carretera vertical

                dibujarSemaforos(g);

                for (Vehiculo vehiculo : vehiculosHorizontales) {
                    g.setColor(vehiculo.color);
                    g.fillRect(vehiculo.posX, vehiculo.posY, 40, 20); // Dibujar el vehículo
                }

                for (Vehiculo vehiculo : vehiculosVerticales) {
                    g.setColor(vehiculo.color);
                    g.fillRect(vehiculo.posX, vehiculo.posY, 20, 40); // Dibujar el vehículo
                }
            }
        };
        panelCarretera.setBackground(Color.DARK_GRAY);
        add(panelCarretera, BorderLayout.CENTER);

        // Panel de control para agregar vehículos y mostrar estadísticas
        JPanel panelControl = new JPanel();
        JButton btnAgregarVehiculoHorizontal = new JButton("Agregar Vehículo Horizontal");
        JButton btnAgregarVehiculoVertical = new JButton("Agregar Vehículo Vertical");

        // Controles para ajustar el tiempo verde de los semáforos
        JLabel lblVerdeHorizontal = new JLabel("Tiempo Verde Horizontal:");
        sliderTiempoVerdeHorizontal = new JSlider(1000, 10000, tiempoMinimoVerde); // Ajustable entre 1 y 10 segundos
        sliderTiempoVerdeHorizontal.setMajorTickSpacing(2000);
        sliderTiempoVerdeHorizontal.setPaintTicks(true);
        sliderTiempoVerdeHorizontal.setPaintLabels(true);

        JLabel lblVerdeVertical = new JLabel("Tiempo Verde Vertical:");
        sliderTiempoVerdeVertical = new JSlider(1000, 10000, tiempoMinimoVerde); // Ajustable entre 1 y 10 segundos
        sliderTiempoVerdeVertical.setMajorTickSpacing(2000);
        sliderTiempoVerdeVertical.setPaintTicks(true);
        sliderTiempoVerdeVertical.setPaintLabels(true);

        // Etiqueta para mostrar estadísticas
        lblEstadisticas = new JLabel();
        actualizarEstadisticas(); // Iniciar con estadísticas vacías

        panelControl.add(btnAgregarVehiculoHorizontal);
        panelControl.add(btnAgregarVehiculoVertical);
        panelControl.add(lblVerdeHorizontal);
        panelControl.add(sliderTiempoVerdeHorizontal);
        panelControl.add(lblVerdeVertical);
        panelControl.add(sliderTiempoVerdeVertical);
        panelControl.add(lblEstadisticas);
        add(panelControl, BorderLayout.SOUTH);

        // Acción del botón para agregar un vehículo horizontal
        btnAgregarVehiculoHorizontal.addActionListener(e -> {
            agregarVehiculoHorizontal();
            actualizarEstadisticas();
        });

        // Acción del botón para agregar un vehículo vertical
        btnAgregarVehiculoVertical.addActionListener(e -> {
            agregarVehiculoVertical();
            actualizarEstadisticas();
        });
        
         Timer timerDB3 = new Timer(5000, e -> {
            ArrayList<VehiculoDB> nuevosVehiculos = dbHelper.obtenerVehiculosDesdeDB();
            for (VehiculoDB vehiculo : nuevosVehiculos) {
                if (vehiculo.getDireccion().equals("horizontal")) {
                    agregarVehiculoHorizontal();  // Agregar desde la base de datos
                    actualizarEstadisticas();     // Actualiza las estadísticas
                } else {
                    agregarVehiculoVertical();    // Agregar desde la base de datos
                    actualizarEstadisticas();     // Actualiza las estadísticas
                }
            }
        });
        timerDB3.start(); // Iniciar el Timer
        
        // Iniciar el control del semáforo y los vehículos
        new Thread(new ControlSemaforo()).start();
    }

    // Método para dibujar los semáforos visuales
    private void dibujarSemaforos(Graphics g) {
        // Semáforo horizontal
        g.setColor(luzVerdeHorizontal ? Color.GREEN : Color.RED);
        g.fillOval(300, 240, 30, 30); // Semáforo para los vehículos horizontales

        // Semáforo vertical
        g.setColor(!luzVerdeHorizontal ? Color.GREEN : Color.RED);
        g.fillOval(420, 190, 30, 30); // Semáforo para los vehículos verticales
    }
DBHelper dbHelper = new DBHelper();

// Timer para revisar la base de datos cada 5 segundos



    // Método para agregar un nuevo vehículo en el flujo horizontal
    private void agregarVehiculoHorizontal() {
        Vehiculo nuevoVehiculo = new Vehiculo(0, 290, getRandomColor(), "horizontal");
        vehiculosHorizontales.add(nuevoVehiculo);

        // Crear un nuevo hilo para el vehículo
        Thread nuevoHiloVehiculo = new Thread(() -> {
            while (true) {
                nuevoVehiculo.mover();
                panelCarretera.repaint(); // Re-pintar la carretera para actualizar el movimiento
                try {
                    Thread.sleep(100); // Ajusta la velocidad de movimiento
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        nuevoHiloVehiculo.start();  // Iniciar el nuevo hilo
    }

    // Método para agregar un nuevo vehículo en el flujo vertical
    private void agregarVehiculoVertical() {
        Vehiculo nuevoVehiculo = new Vehiculo(390, 0, getRandomColor(), "vertical");
        vehiculosVerticales.add(nuevoVehiculo);

        // Crear un nuevo hilo para el vehículo
        Thread nuevoHiloVehiculo = new Thread(() -> {
            while (true) {
                nuevoVehiculo.mover();
                panelCarretera.repaint(); // Re-pintar la carretera para actualizar el movimiento
                try {
                    Thread.sleep(100); // Ajusta la velocidad de movimiento
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        nuevoHiloVehiculo.start();  // Iniciar el nuevo hilo
    }

    // Método para generar colores aleatorios para los vehículos
    private Color getRandomColor() {
        return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    // Método para actualizar las estadísticas de tráfico
    private void actualizarEstadisticas() {
        String estadisticas = "<html>Vehículos en la calle horizontal: " + vehiculosHorizontales.size() + "<br>" +
                "Vehículos en la calle vertical: " + vehiculosVerticales.size() + "<br>" +
                "Vehículos que han pasado en horizontal: " + vehiculosPasaronHorizontal + "<br>" +
                "Vehículos que han pasado en vertical: " + vehiculosPasaronVertical + "</html>";
        lblEstadisticas.setText(estadisticas);
    }

    class Vehiculo {
        int posX, posY;
        Color color;
        String direccion; 
        boolean haPasado; 

        public Vehiculo(int posX, int posY, Color color, String direccion) {
            this.posX = posX;
            this.posY = posY;
            this.color = color;
            this.direccion = direccion;
            this.haPasado = false; // Inicialmente, el vehículo no ha pasado la intersección
        }

        public void mover() {
             // Ejemplo de añadir más trabajo en el hilo
              for (int i = 0; i < 1000000; i++) {
        double result = Math.sin(i) * Math.cos(i); // Cálculo más intensivo
    }
 
            if (direccion.equals("horizontal")) {
                try {
                    semaforoHorizontal.acquire();
                    posX += 5; // Mueve el vehículo horizontalmente
                    if (posX > getWidth()) {
                        posX = -40; 
                        haPasado = false; 
                    }
                    if (!haPasado && posX > getWidth()) {
                        vehiculosPasaronHorizontal++;
                        haPasado = true;
                        actualizarEstadisticas();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaforoHorizontal.release();
                }
            } else if (direccion.equals("vertical")) {
                try {
                    semaforoVertical.acquire();
                    posY += 5; // Mueve el vehículo verticalmente
                    if (posY > getHeight()) {
                        posY = -40;
                        haPasado = false;
                    }
                    if (!haPasado && posY > getHeight()) {
                        vehiculosPasaronVertical++;
                        haPasado = true;
                        actualizarEstadisticas();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaforoVertical.release();
                }
            }
        }
    }
public class DBHelper {
    private Connection connection;

  public DBHelper() {
    try {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Arqui2;user=sa;password=sa123;encrypt=false;";
        connection = DriverManager.getConnection(connectionUrl);
    } catch (ClassNotFoundException e) {
        System.out.println("Driver JDBC no encontrado.");
        e.printStackTrace();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    public ArrayList<VehiculoDB> obtenerVehiculosDesdeDB() {
        ArrayList<VehiculoDB> vehiculos = new ArrayList<>();
        try {
            String query = "SELECT * FROM vehiculos WHERE procesado = 0";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                VehiculoDB vehiculo = new VehiculoDB(rs.getString("direccion"), rs.getString("color"));
                vehiculos.add(vehiculo);
            }

            // Actualiza los vehículos como procesados
            String updateQuery = "UPDATE vehiculos SET procesado = 1 WHERE procesado = 0";
            statement = connection.prepareStatement(updateQuery);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return vehiculos;
        }
        return vehiculos;
    }

    public void insertarVehiculo(String direccion, String color) {
        try {
            String query = "INSERT INTO vehiculos (direccion, color, procesado) VALUES (?, ?, 0)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, direccion);
            statement.setString(2, color);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
public class VehiculoDB {
    public String direccion;
    public String color;

    public VehiculoDB(String direccion, String color) {
        this.direccion = direccion;
        this.color = color;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getColor() {
        return color;
    }
}

    class ControlSemaforo implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // Obtener los valores de los sliders
                    int tiempoVerdeHorizontal = sliderTiempoVerdeHorizontal.getValue();
                    int tiempoVerdeVertical = sliderTiempoVerdeVertical.getValue();

                    // Permitir que los vehículos horizontales se muevan
                    luzVerdeHorizontal = true;
                    semaforoHorizontal.release(vehiculosHorizontales.size()); // Permitir el paso de los vehículos horizontales
                    semaforoVertical.drainPermits(); // Asegurar que los vehículos verticales estén bloqueados
                    repaint();
                    Thread.sleep(tiempoVerdeHorizontal); // Tiempo dinámico en verde para flujo horizontal

                    // Permitir que los vehículos verticales se muevan
                    luzVerdeHorizontal = false;
                    semaforoVertical.release(vehiculosVerticales.size()); // Permitir el paso de los vehículos verticales
                    semaforoHorizontal.drainPermits(); // Asegurar que los vehículos horizontales estén bloqueados
                    repaint();
                    Thread.sleep(tiempoVerdeVertical); // Tiempo dinámico en verde para flujo vertical
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Método principal para ejecutar la simulación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimulacionInterseccionCruzada().setVisible(true);
         
            //timerDB.start();
        });
    }
}
