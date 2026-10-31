package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.File;
import java.util.List;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

public class ReservationExportDaoImpl implements ReservationExportDao {
  private final File file;

  public ReservationExportDaoImpl() {
    this.file = new File("reservations-export.csv");
  }

  // visible for testing, so tests can point at a temp file instead of the real one
  ReservationExportDaoImpl(File file) {
    this.file = file;
  }

  // Ejercicio 10 (Parte A)
  
@Override
public void exportAll(List<Reservation> reservations) throws DataAccessException {
    try (BufferedWriter writer = Files.newBufferedWriter(
            file.toPath(), StandardCharsets.UTF_8)) {

        // Escribir el encabezado del CSV
        writer.write(
            "reservationId,date,ownerUsername,serviceCompany,calculatedPrice"
        );
        writer.newLine();

        // Escribir una fila por cada reserva
        for (Reservation reservation : reservations) {
            String linea = String.format(
                Locale.US,
                "%s,%s,%s,%s,%.2f",
                reservation.getId(),
                reservation.getDate(),
                reservation.getOwner().getUsername(),
                reservation.getService().getCompany(),
                reservation.calculatePrice()
            );

            writer.write(linea);
            writer.newLine();
        }

    } catch (IOException e) {
        throw new DataAccessException(
            "Error al exportar las reservas a CSV", e
        );
    }
  }
}
