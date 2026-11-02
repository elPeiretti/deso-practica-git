package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
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
  // Ejercicio 3 (Parte A)
  @Override
  public List<Reservation> findByDateRange(Instant from, Instant to) throws DataAccessException {
    List<Reservation> result = new java.util.ArrayList<>();
    if (!file.exists()) {
      return result;
    }

    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.trim().isEmpty()) continue;

        String[] data = line.split(",");
        Instant date = Instant.parse(data[3]);

        // Comprobar rango inclusivo (que no sea anterior a 'from' ni posterior a 'to')
        if (!date.isBefore(from) && !date.isAfter(to)) {
          Long id = Long.parseLong(data[0]);
          java.util.UUID serviceId = java.util.UUID.fromString(data[1]);
          java.util.UUID ownerId = java.util.UUID.fromString(data[2]);

          // 1. Instanciar User vacío con Builder usando la ruta completa
          domain.User dummyUser = domain.User.builder().id(ownerId).build();

          // 2. Instanciar TouristService usando la ruta completa
          domain.TouristService dummyService = new domain.TouristService(serviceId, "dummy") {
            @Override
            public double calculatePrice() {
              return 0.0;
            }
          };

          // 3. Construir la reserva (Reservation ya estaba importado en la cabecera)
          Reservation reservation = new Reservation.Builder()
                  .id(id)
                  .service(dummyService)
                  .owner(dummyUser)
                  .date(date)
                  .build();

          result.add(reservation);
        }
      }
    } catch (java.io.IOException e) {
      throw new DataAccessException("Error de lectura E/S en el archivo de reservas", e);
    } catch (RuntimeException e) {
      throw new DataAccessException("Error de formato de datos en el archivo de reservas", e);
    }

    return result;
  }
}
