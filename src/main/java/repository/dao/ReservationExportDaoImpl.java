package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.File;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

      writer.println("reservationId,date,ownerUsername,serviceCompany,calculatedPrice");

      for (Reservation r : reservations) {
        String line = String.format(Locale.US, "%s,%s,%s,%s,%.2f",
                r.getId(),
                r.getDate().toString(),
                r.getOwner().getUsername(),
                r.getService().getCompany(),
                r.calculatePrice()
        );
        writer.println(line);
      }

    } catch (IOException e) {
      throw new DataAccessException("Error exporting reservations to CSV", e);
    }
  }
}
