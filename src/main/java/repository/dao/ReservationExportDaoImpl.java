package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
    try (FileWriter escritor = new FileWriter(file)){
      escritor.write("reservationId,date,ownerUsername,serviceCompany,calculatedPrice\n");
      for (Reservation reserva : reservations){
        escritor.write(reserva.getId().toString());
        escritor.write("," + reserva.getDate().toString());
        escritor.write("," + reserva.getOwner().getUsername());
        escritor.write("," + reserva.getService().getCompany());
        escritor.write("," + String.format(Locale.US, "%.2f", reserva.calculatePrice())+ "\n");

      }

    }catch (IOException e) {
      throw new DataAccessException("IOException",e);
    }
  }
}
