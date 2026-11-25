import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class GestionRecetas {

    public static void main(String[] args) {

        try (Connection con = ConexionBD.getConexion()) {

            System.out.println("Conexión establecida correctamente.");

            mostrarRecetas(con);

        } catch (SQLException e) {

            System.out.println("Error: " + e.getMessage());

        }
    }

    public static void mostrarRecetas(Connection con) throws SQLException {

        String sql = "SELECT * FROM recetas";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Receta receta = new Receta(
                        rs.getInt("tiempo_preparacion"),
                        rs.getString("categoria"),
                        rs.getString("nombre"),
                        rs.getInt("id"),
                        rs.getString("dificultad"),
                        rs.getInt("raciones")
                );

                System.out.println(receta);
            }
        }
    }
}