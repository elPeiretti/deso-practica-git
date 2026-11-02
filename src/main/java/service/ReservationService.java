package service;

import domain.*;
import dto.reservation.CreateReservationRequest;
import dto.reservation.ReservationResponse;
import dto.reservation.ValidationResultDTO;
import dto.user.UserSummaryDTO;
import exception.DataAccessException;
import exception.DuplicateReservationException;
import exception.FlightNotFoundException;
import exception.InvalidDateRangeException;
import exception.UserNotFoundException;
import repository.dao.FlightDao;
import repository.dao.ReservationDao;
import repository.dao.ReservationExportDao;
import repository.dao.UserDao;
import util.Pair;
import java.util.Objects;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ReservationService {
  private final ReservationDao reservationDao;
  private final UserDao userDao;
  private final FlightDao flightDao;

  public ReservationService(ReservationDao reservationDao, UserDao userDao, FlightDao flightDao) {
    this.reservationDao = reservationDao;
    this.userDao = userDao;
    this.flightDao = flightDao;
  }

  public ReservationResponse createReservation(CreateReservationRequest request)
      throws DataAccessException, UserNotFoundException, FlightNotFoundException {
    // 1. Resolver entidades de dominio desde los datos del DTO
    User user =
        userDao
            .findByUsername(request.username())
            .orElseThrow(() -> new UserNotFoundException(request.username()));
    TouristService service =
        flightDao
            .findByNumber(request.flightNumber())
            .orElseThrow(() -> new FlightNotFoundException(request.flightNumber()));

    // 2. Construir la reserva (lógica de dominio)
    Reservation reservation =
        new Reservation.Builder()
            .id((long) (Math.random() * 1000))
            .owner(user)
            .service(service)
            .date(request.date())
            .build();

    reservationDao.save(reservation);

    // 3. Mapear a DTO de respuesta
    return new ReservationResponse(
        reservation.getId(),
        user.getUsername(),
        service.toString(),
        reservation.calculatePrice(),
        request.date());
  }

  // Ejercicio 1
  public List<Reservation> getReservationsByUser(
          List<Reservation> reservations, String username, UserDao userDao)
          throws UserNotFoundException, DataAccessException {
    userDao.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    if (reservations == null) {
      return List.of();
    }
    return reservations.stream()
            .filter(r -> r.getOwner() != null && username.equals(r.getOwner().getUsername()))
            .sorted(Comparator.comparingDouble(Reservation::calculatePrice).reversed())
            .toList();
  }

  // Ejercicio 3
  public List<Reservation> getTopNMostExpensive(
          ReservationDao reservationDao, Instant from, Instant to, int n)
          throws InvalidDateRangeException, DataAccessException {

    if (from == null || to == null || from.isAfter(to)) {
      throw new InvalidDateRangeException("Rango de fechas inválido o nulo");
    }

    if (n <= 0) {
      return java.util.Collections.emptyList();
    }

    return reservationDao.findByDateRange(from, to).stream()
            .sorted(java.util.Comparator.comparing(Reservation::calculatePrice).reversed())
            .limit(n)
            .collect(java.util.stream.Collectors.toList());
  }

  // Ejercicio 5
  private String ubicacionDe(Reservation reservation){
    TouristService service = reservation.getService();
    Location loc = null;
    if(service instanceof Accommodation a){
      loc = a.getLocation();
    } else if (service instanceof Flight f) {
      loc = f.getArrivalInfo();
    }
    if(loc == null){
      return null;
    }
    return loc.getCountry();
  }

  public Map<String, List<Reservation>> groupReservationsByCountry(List<Reservation> reservations) {
    Map<String, List<Reservation>> viaje = reservations.stream()
            .filter(a -> ubicacionDe(a) != null)
            .sorted(Comparator.comparing(Reservation::getDate))
            .collect(Collectors.groupingBy(this::ubicacionDe));
    return viaje;
  }

  // Ejercicio 7
  public Reservation createReservation(
      String username,
      String flightNumber,
      Instant date,
      UserDao userDao,
      FlightDao flightDao,
      ReservationDao reservationDao)
      throws UserNotFoundException,
          FlightNotFoundException,
          InvalidDateRangeException,
          DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 7: implement createReservation");
  }

  // Ejercicio 8
  public Reservation addReservationIfNotDuplicate(
      List<Reservation> existingReservations,
      Reservation newReservation,
      ReservationDao reservationDao)
      throws DuplicateReservationException, DataAccessException {

    boolean duplicado = existingReservations.stream().anyMatch(
            res -> Objects.equals(res.getOwner().getId(), newReservation.getOwner().getId())
                              && Objects.equals(
                                      res.getService().getId(), newReservation.getService().getId()
            ));//El booleano guarda el resultado de comparar la nueva reserva con alguna qu ya exista con el mismo owner e id de servicio

    if(duplicado) {throw new DuplicateReservationException(newReservation.getOwner().getId(), newReservation.getService().getId());}

    reservationDao.save(newReservation);
    return newReservation;

   // throw new UnsupportedOperationException("Ejercicio 8: implement addReservationIfNotDuplicate");
  }

  // Ejercicio 10
  public void exportReservationsToCsv(
      List<Reservation> reservations, ReservationExportDao exportDao) throws DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 10: implement exportReservationsToCsv");
  }

  // Ejercicio 11
  public List<UserSummaryDTO> getUserReservationSummaries(
      List<Reservation> reservations, UserDao userDao)
      throws UserNotFoundException, DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 11: implement getUserReservationSummaries");
  }

  // Ejercicio 12
  public ValidationResultDTO validateReservationCandidates(
      List<Pair<String, String>> candidates, UserDao userDao, FlightDao flightDao)
      throws DataAccessException {
    throw new UnsupportedOperationException(
        "Ejercicio 12: implement validateReservationCandidates");
  }
}
