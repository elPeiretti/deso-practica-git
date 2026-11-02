package repository.dao;

import domain.Flight;
import domain.Reservation;
import domain.TouristService;
import domain.User;
import exception.DataAccessException;
import exception.UserNotFoundException;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

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
    List<Reservation> todasLasReservas = findAll();

    return todasLasReservas.stream()
      .filter(reserva -> {
        Instant fecha = reserva.getDate();
        return (
                (fecha.equals(from) || fecha.isAfter(from))
                 && (fecha.equals(to) || fecha.isBefore(to)));
          })

              .collect(Collectors.toList());
  }




  public List<Reservation> findAll() throws DataAccessException {
    List<Reservation> reservations = new ArrayList<>();



    if (!this.file.exists()) {
      return reservations;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] data = line.split(",");

        if (data.length >= 4) {
          Long reservationId = Long.parseLong(data[0]);
          UUID serviceId = UUID.fromString(data[1]);
          UUID ownerId = UUID.fromString(data[2]);
          Instant date = Instant.parse(data[3]);


          // Recrear objetos básicos solo con sus IDs extraídos del CSV
          Flight flight = new Flight("Vuelo de prueba");
          // Nota: Si tu clase Flight o TouristService requiere el ID explícito, agrégalo:
          // flight.setId(serviceId);

          User user = User.builder().id(ownerId).build();

          Reservation reserva = new Reservation.Builder()
                  .id(reservationId)
                  .service(flight)
                  .owner(user)
                  .date(date)
                  .build();

          reservations.add(reserva);
        }
      }
    } catch (IOException e) {
      throw new DataAccessException("Error al leer el archivo de reservas", e);
    }

    return reservations;
  }

}
