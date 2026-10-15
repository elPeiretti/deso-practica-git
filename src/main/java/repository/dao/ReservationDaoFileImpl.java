package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.*;
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

    if (!file.exists()) {
      return result;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split(",");
        Instant date = Instant.parse(parts[3]);

        if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
          Reservation reservation = new Reservation();
          reservation.setId(Long.parseLong(parts[0]));
          reservation.setDate(date);
          result.add(reservation);
        }
      }
    } catch (IOException e) {
      throw new DataAccessException("Error al leer las reservas", e);
    }

    return result;
  }
}
