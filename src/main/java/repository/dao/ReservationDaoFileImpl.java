package repository.dao;

import domain.Reservation;
import domain.TouristService;
import domain.User;
import exception.DataAccessException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

//Añado estas librerias
import java.util.Map;
import java.util.HashMap;

public class ReservationDaoFileImpl implements ReservationDao {
  private static ReservationDaoFileImpl instance;
  private final File file;

  //Guardo instancias de reservations
  private final Map<String, Reservation> savedReservations = new HashMap<>();
  /////

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
      //Añado que luego de escribir el archivo quede guardada la instancia en mem
      savedReservations.put(reservation.getId(), reservation);
      /////
    } catch (IOException e) {
      throw new DataAccessException("Error al guardar la reserva", e);
    }
  }

  // Ejercicio 3 (Parte A)
  // Ejercicio 3 (Parte A)
  @Override
  public List<Reservation> findByDateRange(Instant from, Instant to) throws DataAccessException {
    List<Reservation> filteredReservations = new java.util.ArrayList<>();

    if (!file.exists()) {
      return filteredReservations;
    }

    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split(",");
        //Me aseguro que la línea tiene id, serviceId, ownerId, date
        if (parts.length < 4) continue;

        Instant date = Instant.parse(parts[3]);

        // Filtro x fecha (en ambos extremos)
        if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
          // Añado que si la reserva fué guardada en la mem, se devuelva esa instancia en vez de llamarla otra vez desde el csv
          Reservation cached = savedReservations.get(parts[0]);
          if (cached != null) {
            filteredReservations.add(cached);
            continue;
          }


          // 1. Convertir el ID de la reserva de String a Long
          Long resId = Long.parseLong(parts[0]);

          // 2. Instanciar un User ficticio y asignarle el ID recuperado como UUID
          User owner = new User();
          owner.setId(java.util.UUID.fromString(parts[2]));

          // 3. Crear el servicio con una clase anónima (ya que TouristService suele ser interfaz)
          TouristService service = new TouristService("nn") {
            @Override
            public String getId() { return parts[1]; }

            @Override
            public double calculatePrice() { return 0.0; } // Irrelevante para este filtro
          };

          // 4. Construir la reserva completa
          Reservation res = new Reservation.Builder()
                  .id(resId)
                  .service(service)
                  .owner(owner)
                  .date(date)
                  .build();

          filteredReservations.add(res);
        }
      }
    } catch (IOException e) {
      throw new DataAccessException("Error leyendo el archivo de reservas", e);
    }

    return filteredReservations;
  }
}
