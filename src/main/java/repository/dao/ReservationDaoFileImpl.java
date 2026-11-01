package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

//Agregado para ejercicio 3
import domain.TouristService;
import domain.User;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.UUID;
// ------------------
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
  @Override
  public List<Reservation> findByDateRange(Instant from, Instant to) throws DataAccessException {
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
            if (line.isBlank()) continue;
            String[] parts = line.split(",");
            Instant date = Instant.parse(parts[3]);
            if (date.isBefore(from) || date.isAfter(to)) continue;

            User owner = User.builder().id(UUID.fromString(parts[2])).build();
            TouristService service = new StoredService(UUID.fromString(parts[1]));
            result.add(
                new Reservation.Builder()
                    .id(Long.parseLong(parts[0]))
                    .date(date)
                    .service(service)
                    .owner(owner)
                    .build());
        }
    } catch (IOException e) {
        throw new DataAccessException("Error al leer las reservas", e);
    }
    return result;
}

// El archivo solo guarda el ID del servicio, así que se reconstruye uno mínimo.
private static class StoredService extends TouristService {
    StoredService(UUID id) {
        super(id, null);
    }

    @Override
    public double calculatePrice() {
        return 0;
    }
}
  }
}
