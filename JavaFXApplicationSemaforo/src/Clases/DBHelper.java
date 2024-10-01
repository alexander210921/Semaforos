package Clases;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;




public class DBHelper {
    private Connection connection;

    public DBHelper() {
        try {
            // Conectar a la base de datos SQL Server
            String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Arqui2;user=sa;password=sa123;";
            connection = DriverManager.getConnection(connectionUrl);
        } catch (Exception e) {
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
