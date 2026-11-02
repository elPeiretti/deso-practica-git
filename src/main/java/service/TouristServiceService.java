package service;

import domain.Flight;
import domain.TouristService;
import exception.FlightNotFoundException;
import exception.ServiceNotFoundException;
import repository.dao.FlightDao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

public class TouristServiceService {

  // Ejercicio 2
  public double getAveragePriceByCompany(List<TouristService> services, String company)
      throws ServiceNotFoundException {
    OptionalDouble average = services.stream()
            .filter(s -> s.getCompany() != null && s.getCompany().equalsIgnoreCase(company))
            .mapToDouble(TouristService::calculatePrice)
            .average();

    if (average.isEmpty()) {
      throw new ServiceNotFoundException(company);
    }

    return average.getAsDouble();
  }

  // Ejercicio 4
  public List<Flight> getFlightsSortedByPriceThenAirline(
      List<String> flightNumbers, FlightDao flightDao) throws FlightNotFoundException {
    List<Flight> flights = new ArrayList<>();
    for (String number : flightNumbers) {
      Flight flight =
          flightDao.findByNumber(number).orElseThrow(() -> new FlightNotFoundException(number));
      flights.add(flight);
    }
    flights.sort(
        Comparator.comparingDouble(Flight::calculatePrice).thenComparing(Flight::getAirline));
    return flights;
  }
}
