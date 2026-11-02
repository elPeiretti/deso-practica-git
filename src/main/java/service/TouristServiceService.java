package service;

import domain.Flight;
import domain.TouristService;
import exception.FlightNotFoundException;
import exception.ServiceNotFoundException;
import repository.dao.FlightDao;

import java.util.List;

public class TouristServiceService {

  // Ejercicio 2
public double getAveragePriceByCompany(List<TouristService> services, String company)
      throws ServiceNotFoundException {
    if (services == null || company == null) {
      throw new IllegalArgumentException("Los servicios y la empresa no deben ser nulos.");
    }

    return services.stream()
        .filter(service -> service.getCompany() != null
            && service.getCompany().equalsIgnoreCase(company))
        .mapToDouble(TouristService::calculatePrice)
        .average()
        .orElseThrow(() -> new ServiceNotFoundException(company));
  }

  // Ejercicio 4
  public List<Flight> getFlightsSortedByPriceThenAirline(
      List<String> flightNumbers, FlightDao flightDao) throws FlightNotFoundException {
    throw new UnsupportedOperationException(
        "Ejercicio 4: implement getFlightsSortedByPriceThenAirline");
  }
}
