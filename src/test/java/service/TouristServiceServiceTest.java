package service;

import domain.Flight;
import exception.FlightNotFoundException;
import exception.ServiceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.dao.FlightDao;
import support.Fixtures;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TouristServiceServiceTest {

  @Mock private FlightDao flightDao;

  private TouristServiceService touristServiceService;

  @BeforeEach
  void setUp() {
    touristServiceService = new TouristServiceService();
  }

  @Nested
  class GetAveragePriceByCompanyTests {

    @Test
    void should_returnAveragePrice_when_companyHasServices() throws Exception {
      Flight flight1 = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Flight flight2 = Fixtures.flight("F2", "AeroLine", "AeroLine", 300, 1);

      double average =
          touristServiceService.getAveragePriceByCompany(List.of(flight1, flight2), "AeroLine");

      double expected = (flight1.calculatePrice() + flight2.calculatePrice()) / 2;
      assertEquals(expected, average);
    }

    @Test
    void should_beCaseInsensitive_when_matchingCompanyName() throws Exception {
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);

      double average = touristServiceService.getAveragePriceByCompany(List.of(flight), "aeroline");

      assertEquals(flight.calculatePrice(), average);
    }

    @Test
    void should_ignoreServicesFromOtherCompanies_when_computingAverage() throws Exception {
      Flight aeroLine = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Flight otherCo = Fixtures.flight("F2", "OtherCo", "OtherCo", 900, 1);

      double average =
          touristServiceService.getAveragePriceByCompany(List.of(aeroLine, otherCo), "AeroLine");

      assertEquals(aeroLine.calculatePrice(), average);
    }

    @Test
    void should_throwServiceNotFoundException_when_noServicesMatchCompany() {
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);

      assertThrows(
          ServiceNotFoundException.class,
          () -> touristServiceService.getAveragePriceByCompany(List.of(flight), "UnknownCo"));
    }

    @Test
    void should_throwServiceNotFoundException_when_servicesListIsEmpty() {
      assertThrows(
          ServiceNotFoundException.class,
          () -> touristServiceService.getAveragePriceByCompany(List.of(), "AeroLine"));
    }
  }

  @Nested
  class GetFlightsSortedByPriceThenAirlineTests {

    @Test
    void should_returnFlightsSortedByPriceAscending_when_pricesDiffer() throws Exception {
      Flight cheap = Fixtures.flight("F1", "AeroLine", "Zeta", 100, 1);
      Flight expensive = Fixtures.flight("F2", "AeroLine", "Alpha", 1000, 1);
      when(flightDao.findByNumber("F1")).thenReturn(Optional.of(cheap));
      when(flightDao.findByNumber("F2")).thenReturn(Optional.of(expensive));

      List<Flight> result =
          touristServiceService.getFlightsSortedByPriceThenAirline(List.of("F2", "F1"), flightDao);

      assertEquals(List.of(cheap, expensive), result);
    }

    @Test
    void should_sortByAirlineAlphabetically_when_pricesAreEqual() throws Exception {
      Flight zeta = Fixtures.flight("F1", "AeroLine", "Zeta", 100, 1);
      Flight alpha = Fixtures.flight("F2", "AeroLine", "Alpha", 100, 1);
      when(flightDao.findByNumber("F1")).thenReturn(Optional.of(zeta));
      when(flightDao.findByNumber("F2")).thenReturn(Optional.of(alpha));

      List<Flight> result =
          touristServiceService.getFlightsSortedByPriceThenAirline(List.of("F1", "F2"), flightDao);

      assertEquals(List.of(alpha, zeta), result);
    }

    @Test
    void should_returnEmptyList_when_flightNumbersListIsEmpty() throws Exception {
      List<Flight> result =
          touristServiceService.getFlightsSortedByPriceThenAirline(List.of(), flightDao);

      assertTrue(result.isEmpty());
    }

    @Test
    void should_throwFlightNotFoundException_when_flightNumberDoesNotExist() {
      when(flightDao.findByNumber("ghost")).thenReturn(Optional.empty());

      assertThrows(
          FlightNotFoundException.class,
          () ->
              touristServiceService.getFlightsSortedByPriceThenAirline(
                  List.of("ghost"), flightDao));
    }
  }
}
