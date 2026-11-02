package service;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
//----------------------------------------------------------------------------------------------------
 // Ejercicio 1      Filtrado y ordenamiento por precio
  public List<Reservation> getReservationsByUser(List<Reservation> reservations, String username, UserDao userDao)
      throws UserNotFoundException, DataAccessException {
      // 1. Validar que el usuario exista (tu código actual)
      userDao.findByUsername(username)
              .orElseThrow(() -> new UserNotFoundException(username));
      //2. filtrar las reservas del usuario
    List<Reservation> reservas=
            reservations.stream()
                            .filter(r ->
                                   r.getOwner()!=null
                                   && r.getOwner().getUsername()!=null
                                   && r.getOwner().getUsername().equals(username))
       // 3. establecer el precio como criterio de comparacion
                    .sorted(Comparator.comparing(Reservation::calculatePrice).reversed())
                    .toList();
  return reservas;  }
  //----------------------------------------------------------------------------------------------------
// Ejercicio 3---------REALIZADO POR GIOVANI MICHELOTTI--------CLASE:RESERVATIONSERVICE------------- 
   public List<Reservation> getTopNMostExpensive(ReservationDao reservationDao, Instant from, Instant to, int n)
      throws InvalidDateRangeException, DataAccessException {
      // Validacion de rango
    if (from == null || to == null || from.isAfter(to)) {
      throw new InvalidDateRangeException("rango de fechas invalido");//verifico el rango
    }
    if (n <= 0) {
      return new ArrayList<Reservation>();  }

    return reservationDao.findByDateRange(from, to)
            .stream()
            .sorted(Comparator.comparing(Reservation::calculatePrice).reversed())
            .limit(n)
            .toList();
    }
    //----------------------------------------------------------------------------------------------------
  // Ejercicio 5
  public Map<String, List<Reservation>> groupReservationsByCountry(List<Reservation> reservations) {
    throw new UnsupportedOperationException("Ejercicio 5: implement groupReservationsByCountry");
  }
  //----------------------------------------------------------------------------------------------------
  // Ejercicio 7 Creacion de reserva con validaciones
  public Reservation createReservation(String username,String flightNumber,Instant date,UserDao userDao,
      FlightDao flightDao,ReservationDao reservationDao)
      throws UserNotFoundException,FlightNotFoundException,
          InvalidDateRangeException,DataAccessException {

User p=userDao.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

TouristService f=flightDao.findByNumber(flightNumber).orElseThrow(() -> new FlightNotFoundException(flightNumber));

if(date!=null){
  if(date.isBefore(Instant.now())){  throw new InvalidDateRangeException(date.toString());}
}else{throw new InvalidDateRangeException("null");}
Reservation r=new Reservation.Builder()
    .owner(p)
    .service(f)
    .date(date)
    .build();
  try {
    reservationDao.save(r);
} catch (DataAccessException e) {
    throw new DataAccessException("Error saving reservation for user: " + username + " and flight: " + flightNumber, e);
} 
return r;
}
  //----------------------------------------------------------------------------------------------------

  // Ejercicio 8
  public Reservation addReservationIfNotDuplicate(
      List<Reservation> existingReservations,
      Reservation newReservation,
      ReservationDao reservationDao)
      throws DuplicateReservationException, DataAccessException {

    boolean duplicado = existingReservations.stream().anyMatch(r ->
           r.getOwner().getId().equals(newReservation.getOwner().getId()) &&
            r.getService().getId().equals(newReservation.getService().getId())
        );

    if (duplicado) {
      throw new DuplicateReservationException(
          String.valueOf(newReservation.getOwner().getId()),
          String.valueOf(newReservation.getService().getId())
      );
    }

    try {
      reservationDao.save(newReservation);
    } catch (DataAccessException e) {
      throw new DataAccessException("Error ", e);
    }

    return newReservation;
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
