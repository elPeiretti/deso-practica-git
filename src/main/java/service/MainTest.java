package service;

import domain.Reservation;
import exception.DataAccessException;
import repository.dao.ReservationDaoFileImpl;

import java.io.File;
import java.time.Instant;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBA DE FILTRO DE FECHAS ===");

        // 1. Instanciar el DAO real
        // Asegúrate de que el archivo 'reservations.csv' exista en la raíz de tu proyecto
        // y tenga datos válidos cargados.
        ReservationDaoFileImpl reservationDao = ReservationDaoFileImpl.getInstance();

        // 2. Definir el rango de fechas que queremos filtrar (por ejemplo, el año 2026)
        Instant from = Instant.parse("2026-08-23T22:25:31.973505999Z");
        Instant to = Instant.parse("2026-08-23T22:25:46.674213101Z");

        System.out.println("Filtrando reservas desde: " + from);
        System.out.println("Hasta: " + to);

        try {
            // 3. Ejecutar el método que queremos probar
            List<Reservation> result = reservationDao.findByDateRange(from, to);

            // 4. Mostrar los resultados
            if (result.isEmpty()) {
                System.out.println("\nNo se encontraron reservas en ese rango de fechas.");
            } else {
                System.out.println("\nReservas encontradas: " + result.size());
                for (Reservation r : result) {
                    System.out.println("- Reserva ID: " + r.getId() + " | Fecha: " + r.getDate());
                }
            }
        } catch (DataAccessException e) {
            System.err.println("Error al acceder a los datos: " + e.getMessage());
        }

        System.out.println("=== PRUEBA FINALIZADA ===");
    }
}