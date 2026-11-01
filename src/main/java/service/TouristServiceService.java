package service;

import domain.Flight;
import domain.TouristService;
import exception.FlightNotFoundException;
import exception.ServiceNotFoundException;
import repository.dao.FlightDao;

import java.util.List;

public class TouristServiceService {

  // Ejercicio 2
  public double getAveragePriceByCompany(List<TouristService> services, String company) throws ServiceNotFoundException {
    double sumaTotalPrecios = 0.0;
    int cantidadServicios = 0;
    for (TouristService servicio : services) {
        if (servicio.getCompany().equalsIgnoreCase(company)) {
            
            // al coincidir, hay que sumar su precio al total y 1 al contador
            sumaTotalPrecios += servicio.calculatePrice();
            cantidadServicios++;
        }
    }
    // aca validamos, osea si el contador sigue en 0, no se encontro la compañia
    if (cantidadServicios == 0) {
        throw new ServiceNotFoundException("No se encontraron servicios para la compañía: " + company);
    }
    return sumaTotalPrecios / cantidadServicios;
}

  // Ejercicio 4
  public List<Flight> getFlightsSortedByPriceThenAirline(
      List<String> flightNumbers, FlightDao flightDao) throws FlightNotFoundException {
    throw new UnsupportedOperationException(
        "Ejercicio 4: implement getFlightsSortedByPriceThenAirline");
  }
}
