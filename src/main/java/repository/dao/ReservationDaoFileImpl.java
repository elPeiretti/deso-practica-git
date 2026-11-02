package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ReservationDaoFileImpl implements ReservationDao {
  private static ReservationDaoFileImpl instance;
  private final File file;

  private ReservationDaoFileImpl() {
    this.file = new File("reservations.csv");
  }

  // visible for testing, so tests can point at a temp file instead of the real one
  ReservationDaoFileImpl(File file) {
    this.file = file;
  }

  // singleton
  public static synchronized ReservationDaoFileImpl getInstance() {
    if (instance == null) {
      instance = new ReservationDaoFileImpl();
    }

    return instance;
  }

  @Override
  public void save(Reservation reservation) throws DataAccessException {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
      String line =
          String.format(
              "%s,%s,%s,%s\n",
              reservation.getId(),
              reservation.getService().getId(),
              reservation.getOwner().getId(),
              reservation.getDate());
      bw.write(line);
    } catch (IOException e) {
      throw new DataAccessException("Error al guardar la reserva", e);
    }
  }

  // Ejercicio 3 (Parte A)
  @Override
  public List<Reservation> findByDateRange(Instant from, Instant to) throws DataAccessException {
    
    List<Reservation> result = new ArrayList<>();
    try {
      for (String line : Files.readAllLines(file.toPath())) {
        String[] p = line.split(",");
        if (p.length < 4) continue;
        Instant date = Instant.parse(p[3].trim());
        if (!date.isBefore(from) && !date.isAfter(to)) {
          Reservation r = new Reservation();
          r.setId(Long.parseLong(p[0].trim()));
          r.setDate(date);
          result.add(r);
        }
      }
    } catch (NoSuchFileException e) {
      return result;                       // file doesn't exist -> empty list
    } catch (IOException e) {
      throw new DataAccessException("Error al leer reservas", e);
    }
    return result;


  }
}
