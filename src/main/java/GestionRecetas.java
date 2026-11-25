import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.util.Scanner;

public class GestionRecetas {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int opcion;

        try (Connection con = ConexionBD.getConexion()) {

            System.out.println("Conexión establecida correctamente.");

            do {
                System.out.println("\n--- GESTOR DE RECETAS ---");
                System.out.println("1. Mostrar todas las recetas");
                System.out.println("2. Buscar receta por ID");
                System.out.println("3. Añadir receta");
                System.out.println("4. Modificar receta");
                System.out.println("5. Eliminar receta");
                System.out.println("6. Buscar receta por nombre");
                System.out.println("0. Salir");
                System.out.print("Introduce una opción: ");

                opcion = sc.nextInt();
                sc.nextLine();

                switch (opcion) {
                    case 1:
                        mostrarRecetas(con);
                        break;
                    case 2:
                        buscarRecetaID(con, sc);
                        break;
                    case 3:
                        anadirReceta(con, sc);
                        break;
                    case 4:
                        modificarReceta(con, sc);
                        break;
                    case 5:
                        eliminarReceta(con, sc);
                        break;
                    case 6:
                        buscarRecetaNombre(con, sc);
                        break;
                    case 0:
                        System.out.println("Saliendo del programa...");
                        break;
                    default:
                        System.out.println("Opción incorrecta.");
                }

            } while (opcion != 0);

        } catch (SQLException e) {

            System.out.println("Error: " + e.getMessage());

        }

        sc.close();
    }


    // 1. MOSTRAR TODAS LAS RECETAS

    public static void mostrarRecetas(Connection con) {

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

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    // 2. BUSCAR RECETA POR ID

    public static void buscarRecetaID(Connection con, Scanner sc) {

        System.out.print("Introduce el ID de la receta: ");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "SELECT * FROM recetas WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    Receta receta = new Receta(
                            rs.getInt("tiempo_preparacion"),
                            rs.getString("categoria"),
                            rs.getString("nombre"),
                            rs.getInt("id"),
                            rs.getString("dificultad"),
                            rs.getInt("raciones")
                    );

                    System.out.println(receta);

                } else {
                    System.out.println("No se ha encontrado la receta.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    // 3. AÑADIR RECETA MEDIANTE UNA TRANSACCIÓN

    public static void anadirReceta(Connection con, Scanner sc)
            throws SQLException {

        System.out.print("Introduce el nombre: ");
        String nombre = sc.nextLine();

        System.out.print("Introduce la categoría: ");
        String categoria = sc.nextLine();

        System.out.print("Introduce el tiempo de preparación: ");
        int tiempoPreparacion = sc.nextInt();
        sc.nextLine();

        System.out.print("Introduce la dificultad: ");
        String dificultad = sc.nextLine();

        System.out.print("Introduce el número de raciones: ");
        int raciones = sc.nextInt();
        sc.nextLine();

        String sql = "INSERT INTO recetas " +
                "(nombre, categoria, tiempo_preparacion, dificultad, raciones) " +
                "VALUES (?, ?, ?, ?, ?)";

        String sqlEstadisticas = "UPDATE estadisticas " +
                "SET total_recetas = total_recetas + 1 WHERE id = 1";

        // Desactivamos la confirmación automática
        con.setAutoCommit(false);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             PreparedStatement pstmtEstadisticas =
                     con.prepareStatement(sqlEstadisticas)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, categoria);
            pstmt.setInt(3, tiempoPreparacion);
            pstmt.setString(4, dificultad);
            pstmt.setInt(5, raciones);

            pstmt.executeUpdate();

            int filas = pstmtEstadisticas.executeUpdate();

            if (filas != 1) {
                throw new SQLException("No se han actualizado las estadísticas.");
            }

            // Confirmamos las dos operaciones
            con.commit();

            System.out.println("Receta añadida correctamente.");

        } catch (SQLException e) {

            // Deshacemos los cambios si se produce un error
            con.rollback();

            System.out.println("No se ha podido añadir la receta.");
            System.out.println("Error: " + e.getMessage());
        }

        // Volvemos a activar la confirmación automática
        con.setAutoCommit(true);
    }


    // 4. MODIFICAR RECETA

    public static void modificarReceta(Connection con, Scanner sc) {

        System.out.print("Introduce el ID de la receta: ");
        int id = sc.nextInt();

        System.out.print("Introduce el nuevo tiempo de preparación: ");
        int tiempoPreparacion = sc.nextInt();

        System.out.print("Introduce el nuevo número de raciones: ");
        int raciones = sc.nextInt();
        sc.nextLine();

        String sql = "UPDATE recetas " +
                "SET tiempo_preparacion = ?, raciones = ? WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, tiempoPreparacion);
            pstmt.setInt(2, raciones);
            pstmt.setInt(3, id);

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                System.out.println("Receta modificada correctamente.");
            } else {
                System.out.println("No se ha encontrado la receta.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    // 5. ELIMINAR RECETA

    public static void eliminarReceta(Connection con, Scanner sc)
            throws SQLException {

        System.out.print("Introduce el ID de la receta: ");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM recetas WHERE id = ?";

        String sqlEstadisticas = "UPDATE estadisticas " +
                "SET total_recetas = total_recetas - 1 WHERE id = 1";

        con.setAutoCommit(false);

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             PreparedStatement pstmtEstadisticas =
                     con.prepareStatement(sqlEstadisticas)) {

            pstmt.setInt(1, id);

            int filas = pstmt.executeUpdate();

            if (filas > 0) {

                int filasEstadisticas = pstmtEstadisticas.executeUpdate();

                if (filasEstadisticas != 1) {
                    throw new SQLException("No se han actualizado las estadísticas.");
                }

                con.commit();

                System.out.println("Receta eliminada correctamente.");

            } else {

                con.rollback();

                System.out.println("No se ha encontrado la receta.");
            }

        } catch (SQLException e) {

            con.rollback();

            System.out.println("Error: " + e.getMessage());
        }

        con.setAutoCommit(true);
    }


    // 6. BUSCAR RECETA POR NOMBRE CON CALLABLESTATEMENT

    public static void buscarRecetaNombre(Connection con, Scanner sc) {

        System.out.print("Introduce el nombre de la receta: ");
        String nombre = sc.nextLine();

        String sql = "{CALL buscarReceta(?)}";

        try (CallableStatement cstmt = con.prepareCall(sql)) {

            cstmt.setString(1, nombre);

            try (ResultSet rs = cstmt.executeQuery()) {

                boolean encontrada = false;

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

                    encontrada = true;
                }

                if (!encontrada) {
                    System.out.println("No se ha encontrado la receta.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}