package service;

import domain.Accommodation;
import domain.Flight;
import domain.Location;
import java.util.stream.Collectors;

import domain.Reservation;
import domain.TouristService;
import domain.User;
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
import java.util.Comparator;

import java.time.Instant;

import java.util.List;
import java.util.Map;

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
      if (userDao.findByUsername(username).isEmpty()){
         throw new UserNotFoundException("No se encontro el usuario" + username);
        }
      List<Reservation> reservas = reservations.stream().filter(r -> r.getOwner().getUsername().equals(username)).sorted( (r1,r2) -> Double.compare(r2.calculatePrice(), r1.calculatePrice())).toList();
      return reservas;      
    //throw new UnsupportedOperationException("Ejercicio 1: implement getReservationsByUser");
  }

  // Ejercicio 3
  public List<Reservation> getTopNMostExpensive(
          ReservationDao reservationDao, Instant from, Instant to, int n)
          throws InvalidDateRangeException, DataAccessException {
    if (from == null || to == null) {
      throw new InvalidDateRangeException("Las fechas 'from' y 'to' son obligatorias");
    }
    if (from.isAfter(to)) {
      throw new InvalidDateRangeException("'from' no puede ser posterior a 'to'");
    }
    if (n <= 0) {
      return List.of();
    }

    return reservationDao.findByDateRange(from, to).stream()
            .sorted(Comparator.comparingDouble(Reservation::calculatePrice).reversed())
            .limit(n)
            .toList();
  }

  // Ejercicio 5
  public Map<String, List<Reservation>> groupReservationsByCountry(List<Reservation> reservations) {
    if (reservations == null) {
      return Map.of();
    }

    return reservations.stream()
        .filter(r -> getCountry(r) != null)
        .sorted(Comparator.comparing(Reservation::getDate))
        .collect(Collectors.groupingBy(this::getCountry));
  }

  private String getCountry(Reservation reservation) {
    if (reservation == null || reservation.getService() == null) {
      return null;
    }
    Location location = null;
    if (reservation.getService() instanceof Accommodation accommodation) {
      location = accommodation.getLocation();
    } else if (reservation.getService() instanceof Flight flight) {
      location = flight.getArrivalInfo();
    }
    return (location != null) ? location.getCountry() : null;
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
    // validar que el usuario exista
    User user =
            userDao.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));

    // validar que el vuelo exista
    TouristService service =
            flightDao
                    .findByNumber(flightNumber)
                    .orElseThrow(() -> new FlightNotFoundException(flightNumber));

    // validar la fecha
    if (date == null || date.isBefore(Instant.now())) {
      throw new InvalidDateRangeException(
              "La fecha de la reserva no puede ser nula ni anterior a la fecha actual");
    }

    // crear la reserva
    Reservation reservation = new Reservation.Builder()
            .id((long) (Math.random()*100))
            .owner(user)
            .date(date)
            .service(service)
            .build();

    // si save falla no debe propagarse
    try {
      reservationDao.save(reservation);
    } catch (DataAccessException e) {
      throw new DataAccessException(
              "Error al persistir la reserva del usuario "
                      + username
                      + " para el vuelo "
                      + flightNumber,
              e);
    }

    return reservation;


  }

  // Ejercicio 8
  public Reservation addReservationIfNotDuplicate(
      List<Reservation> existingReservations,
      Reservation newReservation,
      ReservationDao reservationDao)
      throws DuplicateReservationException, DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 8: implement addReservationIfNotDuplicate");
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
