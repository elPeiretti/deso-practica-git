package service;

import domain.Flight;
import domain.TouristService;
import exception.FlightNotFoundException;
import exception.ServiceNotFoundException;
import repository.dao.FlightDao;

import java.util.ArrayList;
import java.util.List;

public class TouristServiceService {

// Ejercicio 2----REALIZADO POR GIOVANI MICHELOTTI ------------------
  public double getAveragePriceByCompany(List<TouristService> services, String company)
      throws ServiceNotFoundException {
        if(services==null || company==null){ //verifico compania y servicio
          throw new ServiceNotFoundException("compania invalida o listade servicios vacia");
        }
        return services.stream()
        .filter(service ->service.getCompany()!=null && //verifico que la compania no sea null
        service.getCompany().equalsIgnoreCase(company)) //tomo la compania
        .mapToDouble(TouristService::calculatePrice)//calculo el precio y lo paso a double para poder hacer el promedio
        .average()//te hago el promedio,para evitar division por 0
        .orElseThrow(()->new ServiceNotFoundException("no encontramos servicios para la compania" + company));//asumi que a lo mejor no pueda haber servicios en la empresa VER
   }

  
   // Ejercicio 4----REALIZADO POR GIOVANI MICHELOTTI ------------------
  public List<Flight> getFlightsSortedByPriceThenAirline(
      List<String> flightNumbers, FlightDao flightDao) throws FlightNotFoundException {

        if(flightNumbers==null || flightNumbers.isEmpty()){
          return java.util.Collections.emptyList(); //verifico el numero y la coleccion 
        }
        List<Flight> enrichedFlights = new ArrayList<>(); //armo la lista


        for (String number : flightNumbers) {
            Flight flight = flightDao.findByNumber(number) //busco el vuelo y el numero de manera manual
                .orElseThrow(() -> new FlightNotFoundException(number));
            enrichedFlights.add(flight); //armo la lista 
        }
        
        enrichedFlights.sort(java.util.Comparator.comparing(Flight::calculatePrice)
        .thenComparing(Flight::getAirline));//la ordeno usando sort 

        return enrichedFlights;
      
      
      
  }
}
