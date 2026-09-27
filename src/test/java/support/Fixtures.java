package support;

import domain.Accommodation;
import domain.Flight;
import domain.Location;
import domain.Reservation;
import domain.TouristService;
import domain.User;

import java.time.Instant;
import java.util.UUID;

public final class Fixtures {

  private Fixtures() {}

  public static Location location(String country, String city) {
    return new Location(UUID.randomUUID().toString(), country, city, "Main St 123", "Home");
  }

  public static User user(String username) {
    return user(username, null);
  }

  public static User user(String username, Location address) {
    return User.builder()
        .id(UUID.randomUUID())
        .username(username)
        .name(username)
        .address(address)
        .build();
  }

  public static Flight flight(
      String number, String company, String airline, double length, double litersPerKm) {
    Flight flight = new Flight(company);
    flight.setNumber(number);
    flight.setAirline(airline);
    flight.setLength(length);
    flight.setAircraft(new Flight.Aircraft("Aircraft-" + number, litersPerKm));
    return flight;
  }

  public static Flight flightWithArrival(
      String number,
      String company,
      String airline,
      double length,
      double litersPerKm,
      Location arrival) {
    Flight flight = flight(number, company, airline, length, litersPerKm);
    flight.setArrivalInfo(arrival);
    return flight;
  }

  public static Accommodation accommodation(
      String company, double pricePerNight, int guestCount, int nightsCount, Location location) {
    Accommodation accommodation =
        new Accommodation(company, pricePerNight, guestCount, nightsCount);
    accommodation.setLocation(location);
    return accommodation;
  }

  public static Reservation reservation(Long id, Instant date, TouristService service, User owner) {
    return new Reservation.Builder().id(id).date(date).service(service).owner(owner).build();
  }
}
