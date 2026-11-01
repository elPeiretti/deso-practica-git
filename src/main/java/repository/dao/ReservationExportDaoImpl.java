package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.BufferedWriter;
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

    //El bloque try-with-resources abre el archivo en modo escritura y garantiza que se cierre automáticamente al terminar
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {

      //Escribe la primera fila del CSV y da un salto de linea
      writer.write("reservationId,date,ownerUsername,serviceCompany,calculatedPrice");
      writer.newLine();

      //Verificamos que la lista exista y no este vacia
      if (reservations != null && !reservations.isEmpty()) {

        //Recorremos esa lista
        for (Reservation res : reservations) {

          //Arma la fila completa
          String dateIso = res.getDate().toString();
          String price = String.format(Locale.US, "%.2f", res.calculatePrice());

          String line = String.format("%s,%s,%s,%s,%s",
                  res.getId(),
                  dateIso,
                  res.getOwner().getUsername(),
                  res.getService().getCompany(),
                  price
          );

          //Graba la cadena de texto final en el archivo y prepara el siguiente renglón
          writer.write(line);
          writer.newLine();
        }
      }
    //Captura cualquier falla y lanza excepcion
    } catch (IOException e) {
      throw new DataAccessException("Error al exportar reservas a CSV", e);
    }
  }
}
