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
      
        //EJERCICIO 2:
      
        double total = 0;
        int count = 0;

        for (TouristService service : services) {
            if (service.getCompany().equalsIgnoreCase(company)) {
                total += service.calculatePrice();
                count++;
            }
        }

        if (count == 0) {
            throw new ServiceNotFoundException("No se encontraron servicios para la compañía");
        }

        return total / count;
    }

  // Ejercicio 4
  public List<Flight> getFlightsSortedByPriceThenAirline(
      List<String> flightNumbers, FlightDao flightDao) throws FlightNotFoundException {
    throw new UnsupportedOperationException(
        "Ejercicio 4: implement getFlightsSortedByPriceThenAirline");
  }
}
