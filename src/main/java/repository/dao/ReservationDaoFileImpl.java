package repository.dao;

import domain.Reservation;
import domain.TouristService;
import domain.User;
import exception.DataAccessException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    // Si todavía no se guardó ninguna reserva, el archivo no existe
    if (!file.exists()) {
      return result;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }

        try {
          // Formato de cada línea: reservationId,serviceId,ownerId,date
          String[] data = line.split(",");
          Instant date = Instant.parse(data[3].trim());

          // Rango inclusive en ambos extremos
          if (!date.isBefore(from) && !date.isAfter(to)) {
            result.add(toReservation(data, date));
          }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | DateTimeParseException e) {
          // Línea mal formada: se ignora y se sigue con la siguiente
        }
      }
    } catch (IOException e) {
      throw new DataAccessException("Error al leer las reservas", e);
    }

    return result;
  }

  // El archivo solo guarda los IDs del servicio y del usuario, por eso la reserva
  // se reconstruye con objetos que únicamente conocen su ID.
  private Reservation toReservation(String[] data, Instant date) {
    User owner = User.builder().id(UUID.fromString(data[2].trim())).build();
    TouristService service =
            new TouristService(UUID.fromString(data[1].trim()), "unknown") {
              @Override
              public double calculatePrice() {
                return 0;
              }
            };

    return new Reservation.Builder()
            .id(Long.parseLong(data[0].trim()))
            .owner(owner)
            .service(service)
            .date(date)
            .build();
  }
}