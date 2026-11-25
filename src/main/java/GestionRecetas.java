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

            System.out.println("Conexión establecida correctamente.\n");

            do {
                System.out.println("  GESTOR DE RECETAS EVA LOZANO     ");
                System.out.println("--------------------------------");
                System.out.println("1. Mostrar todas las recetas");
                System.out.println("2. Buscar receta por ID");
                System.out.println("3. Añadir receta");
                System.out.println("4. Modificar receta");
                System.out.println("5. Eliminar receta");
                System.out.println("6. Buscar receta por nombre");
                System.out.println("0. Salir");
                System.out.println("--------------------------------");
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
                        System.out.println("\nHAS SELECCIONADO SALIR");
                        System.out.println("Saliendo del programa...");
                        break;
                    default:
                        System.out.println("\nERROR: Opción incorrecta.");
                }

            } while (opcion != 0);

        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        sc.close();
    }


    // METODO PARA MOSTRAR LOS DATOS DE UNA RECETA

    public static void mostrarDatosReceta(Receta receta) {

        System.out.println(" " + receta.getNombre().toUpperCase());
        System.out.println(" - CATEGORÍA: " + receta.getCategoria());
        System.out.println(" - TIEMPO DE PREPARACIÓN: " +
                receta.getTiempoPreparacion() + " minutos");
        System.out.println(" - NIVEL DE DIFICULTAD: " +
                receta.getDificultad());
        System.out.println(" - CANTIDAD DE RACIONES: " +
                receta.getRaciones());
    }


    // 1. MOSTRAR TODAS LAS RECETAS

    public static void mostrarRecetas(Connection con) {

        System.out.println("\nHAS SELECCIONADO MOSTRAR TODAS LAS RECETAS");

        String sql = "SELECT * FROM recetas";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int contador = 0;

            while (rs.next()) {

                Receta receta = new Receta(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getInt("tiempo_preparacion"),
                        rs.getString("dificultad"),
                        rs.getInt("raciones")
                );

                System.out.println("\nReceta (" + receta.getId() + ")");
                mostrarDatosReceta(receta);

                contador++;
            }

            System.out.println("\n--------------------------------");
            System.out.println("TOTAL DE RECETAS: " + contador);

        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }


    // 2. BUSCAR RECETA POR ID

    public static void buscarRecetaID(Connection con, Scanner sc) {

        System.out.println("\nHAS SELECCIONADO BUSCAR RECETA POR ID");

        System.out.print("Introduce el ID de la receta: ");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "SELECT * FROM recetas WHERE id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {

                    Receta receta = new Receta(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getInt("tiempo_preparacion"),
                            rs.getString("dificultad"),
                            rs.getInt("raciones")
                    );

                    System.out.println("\nReceta buscada (" +
                            receta.getId() + ")");

                    mostrarDatosReceta(receta);

                } else {
                    System.out.println("\nNo se ha encontrado ninguna receta con ID " + id + ".");
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }


    // 3. AÑADIR RECETA MEDIANTE UNA TRANSACCIÓN

    public static void anadirReceta(Connection con, Scanner sc)
            throws SQLException {

        System.out.println("\nHAS SELECCIONADO AÑADIR UNA RECETA");

        System.out.print("Introduce el nombre: ");
        String nombre = sc.nextLine();

        System.out.print("Introduce la categoría: ");
        String categoria = sc.nextLine();

        System.out.print("Introduce el tiempo de preparación (minutos): ");
        int tiempoPreparacion = sc.nextInt();
        sc.nextLine();

        System.out.print("Introduce el nivel de dificultad: ");
        String dificultad = sc.nextLine();

        System.out.print("Introduce la cantidad de raciones: ");
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

            System.out.println("\nRECETA AÑADIDA CORRECTAMENTE");

            System.out.println("\nNueva receta:");
            System.out.println(" " + nombre.toUpperCase());
            System.out.println(" - CATEGORÍA: " + categoria);
            System.out.println(" - TIEMPO DE PREPARACIÓN: " +
                    tiempoPreparacion + " minutos");
            System.out.println(" - NIVEL DE DIFICULTAD: " + dificultad);
            System.out.println(" - CANTIDAD DE RACIONES: " + raciones);

        } catch (SQLException e) {

            // Deshacemos los cambios si se produce un error
            con.rollback();

            System.out.println("\nNO SE HA PODIDO AÑADIR LA RECETA");
            System.out.println("ERROR: " + e.getMessage());
        }

        // Volvemos a activar la confirmación automática
        con.setAutoCommit(true);
    }


    // 4. MODIFICAR RECETA

    public static void modificarReceta(Connection con, Scanner sc) {

        System.out.println("\nHAS SELECCIONADO MODIFICAR UNA RECETA");

        System.out.print("Introduce el ID de la receta: ");
        int id = sc.nextInt();
        sc.nextLine();

        String consulta = "SELECT * FROM recetas WHERE id = ?";

        String sql = "UPDATE recetas " +
                "SET tiempo_preparacion = ?, raciones = ? WHERE id = ?";

        try (PreparedStatement pstmtConsulta = con.prepareStatement(consulta);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmtConsulta.setInt(1, id);

            try (ResultSet rs = pstmtConsulta.executeQuery()) {

                if (rs.next()) {

                    Receta receta = new Receta(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getInt("tiempo_preparacion"),
                            rs.getString("dificultad"),
                            rs.getInt("raciones")
                    );

                    System.out.println("\nReceta seleccionada (" +
                            receta.getId() + ")");

                    System.out.println("\nINTRODUCE LOS NUEVOS DATOS");

                    System.out.print("Introduce el nuevo tiempo de preparación " + "(tiempo actual: " + receta.getTiempoPreparacion() + " minutos): ");
                    int tiempoPreparacion = sc.nextInt();

                    System.out.print("Introduce la nueva cantidad de raciones " + "(raciones actuales: " + receta.getRaciones() + "): ");

                    int raciones = sc.nextInt();
                    sc.nextLine();

                    pstmt.setInt(1, tiempoPreparacion);
                    pstmt.setInt(2, raciones);
                    pstmt.setInt(3, id);

                    int filas = pstmt.executeUpdate();

                    if (filas > 0) {

                        System.out.println("\nRECETA MODIFICADA CORRECTAMENTE");

                        System.out.println("\nReceta modificada (" + id + ")");
                        System.out.println(" " + receta.getNombre().toUpperCase());

                        System.out.println(" - CATEGORÍA: " + receta.getCategoria());

                        System.out.println(" - TIEMPO DE PREPARACIÓN: " + receta.getTiempoPreparacion() + " --> " + tiempoPreparacion + " minutos");

                        System.out.println(" - NIVEL DE DIFICULTAD: " + receta.getDificultad());

                        System.out.println(" - CANTIDAD DE RACIONES: " + receta.getRaciones() + " --> " + raciones);

                    } else {
                        System.out.println("\nNo se ha modificado la receta.");
                    }

                } else {
                    System.out.println("\nNo se ha encontrado ninguna receta con ID " + id + ".");
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }


    // 5. ELIMINAR RECETA

    public static void eliminarReceta(Connection con, Scanner sc)
            throws SQLException {

        System.out.println("\nHAS SELECCIONADO ELIMINAR UNA RECETA");

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

                System.out.println("\nRECETA ELIMINADA CORRECTAMENTE");
                System.out.println(" - ID DE LA RECETA ELIMINADA: " + id);
                System.out.println(" - ESTADÍSTICAS: Actualizadas correctamente");

            } else {

                con.rollback();

                System.out.println("\nNo se ha encontrado ninguna receta con ID " + id + ".");
            }

        } catch (SQLException e) {

            con.rollback();

            System.out.println("\nNO SE HA PODIDO ELIMINAR LA RECETA");
            System.out.println("ERROR: " + e.getMessage());
        }

        con.setAutoCommit(true);
    }


    // 6. BUSCAR RECETA POR NOMBRE CON CALLABLESTATEMENT

    public static void buscarRecetaNombre(Connection con, Scanner sc) {

        System.out.println("\nHAS SELECCIONADO BUSCAR RECETA POR NOMBRE");

        System.out.print("Introduce el nombre de la receta: ");
        String nombre = sc.nextLine();

        String sql = "{CALL buscarReceta(?)}";

        try (CallableStatement cstmt = con.prepareCall(sql)) {

            cstmt.setString(1, nombre);

            try (ResultSet rs = cstmt.executeQuery()) {

                boolean encontrada = false;

                while (rs.next()) {

                    Receta receta = new Receta(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getInt("tiempo_preparacion"),
                            rs.getString("dificultad"),
                            rs.getInt("raciones")
                    );

                    System.out.println("\nReceta buscada (" + receta.getId() + ")");

                    mostrarDatosReceta(receta);

                    encontrada = true;
                }

                if (!encontrada) {
                    System.out.println("\nNo se ha encontrado la receta " + nombre.toUpperCase() + ".");
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}