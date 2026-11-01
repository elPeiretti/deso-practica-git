package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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

    try (PrintWriter writer = new PrintWriter(new FileWriter(this.file))) {
        
        writer.println("reservationId,date,ownerUsername,serviceCompany,calculatedPrice");
        
        if (reservations != null) {
            for (Reservation res : reservations) {

                String precio = String.format(java.util.Locale.US, "%.2f", res.calculatePrice());
                
                String fechaIso = res.getDate().toString(); 
                
                writer.printf("%s,%s,%s,%s,%s%n",
                        res.getId(),
                        fechaIso,
                        res.getOwner().getUsername(),
                        res.getService().getCompany(),
                        precio
                );
            }
        }
    } catch (IOException e) {
        throw new DataAccessException("Error al escribir el CSV", e);
    }
}
}
