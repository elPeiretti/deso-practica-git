package service;

import domain.Flight;
import domain.TouristService;
import exception.FlightNotFoundException;
import exception.ServiceNotFoundException;
import repository.dao.FlightDao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TouristServiceService {

  // Ejercicio 2
  public double getAveragePriceByCompany(List<TouristService> services, String company)
      throws ServiceNotFoundException {
    double sumaTotal = 0.0;
    int cantidad = 0;

    for (TouristService service : services) {
      if (service.getCompany().equalsIgnoreCase(company)) {
        sumaTotal += service.calculatePrice();
        cantidad++;
      }
    }

    if (cantidad == 0) {
      throw new ServiceNotFoundException(company);
    }

    return sumaTotal / cantidad;
  }

  // Ejercicio 4
  public List<Flight> getFlightsSortedByPriceThenAirline(
      List<String> flightNumbers, FlightDao flightDao) throws FlightNotFoundException {
    List<Flight> flights = new ArrayList<>();
    for (String flightNumber : flightNumbers) {
      Flight flight =
          flightDao
              .findByNumber(flightNumber)
              .orElseThrow(() -> new FlightNotFoundException(flightNumber));
      flights.add(flight);
    }

    return flights.stream()
        .sorted(Comparator.comparing(Flight::calculatePrice).thenComparing(Flight::getAirline))
        .toList();
  }
}
