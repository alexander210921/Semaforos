package Clases;
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
