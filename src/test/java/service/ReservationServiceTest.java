package service;

import domain.Flight;
import domain.Reservation;
import domain.User;
import dto.reservation.ReservationCandidateDTO;
import dto.reservation.ValidationResultDTO;
import dto.user.UserSummaryDTO;
import exception.DataAccessException;
import exception.DuplicateReservationException;
import exception.FlightNotFoundException;
import exception.InvalidDateRangeException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.dao.FlightDao;
import repository.dao.ReservationDao;
import repository.dao.ReservationExportDao;
import repository.dao.UserDao;
import support.Fixtures;
import util.Pair;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

  @Mock private UserDao userDao;
  @Mock private FlightDao flightDao;
  @Mock private ReservationDao reservationDao;
  @Mock private ReservationExportDao exportDao;

  private ReservationService reservationService;

  @BeforeEach
  void setUp() {
    reservationService = new ReservationService(reservationDao, userDao, flightDao);
  }

  @Nested
  class GetReservationsByUserTests {

    @Test
    void should_returnReservationsSortedByPriceDescending_when_userHasMultipleReservations()
        throws Exception {
      User user = Fixtures.user("fede");
      Flight cheap = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Flight expensive = Fixtures.flight("F2", "AeroLine", "AeroLine", 1000, 1);
      Reservation cheapReservation = Fixtures.reservation(1L, Instant.now(), cheap, user);
      Reservation expensiveReservation = Fixtures.reservation(2L, Instant.now(), expensive, user);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));

      List<Reservation> result =
          reservationService.getReservationsByUser(
              List.of(cheapReservation, expensiveReservation), "fede", userDao);

      assertEquals(List.of(expensiveReservation, cheapReservation), result);
    }

    @Test
    void should_excludeReservationsFromOtherUsers_when_listContainsMultipleUsers()
        throws Exception {
      User fede = Fixtures.user("fede");
      User ana = Fixtures.user("ana");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation fedeReservation = Fixtures.reservation(1L, Instant.now(), flight, fede);
      Reservation anaReservation = Fixtures.reservation(2L, Instant.now(), flight, ana);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(fede));

      List<Reservation> result =
          reservationService.getReservationsByUser(
              List.of(fedeReservation, anaReservation), "fede", userDao);

      assertEquals(List.of(fedeReservation), result);
    }

    @Test
    void should_returnEmptyList_when_reservationsListIsEmpty() throws Exception {
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(Fixtures.user("fede")));

      List<Reservation> result =
          reservationService.getReservationsByUser(List.of(), "fede", userDao);

      assertTrue(result.isEmpty());
    }

    @Test
    void should_throwUserNotFoundException_when_userDoesNotExist() throws Exception {
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

      assertThrows(
          UserNotFoundException.class,
          () -> reservationService.getReservationsByUser(List.of(), "ghost", userDao));
    }

    @Test
    void should_propagateDataAccessException_when_userDaoFails() throws Exception {
      when(userDao.findByUsername("fede")).thenThrow(new DataAccessException("boom"));

      assertThrows(
          DataAccessException.class,
          () -> reservationService.getReservationsByUser(List.of(), "fede", userDao));
    }
  }

  @Nested
  class GetTopNMostExpensiveTests {

    private final Instant from = Instant.parse("2026-01-01T00:00:00Z");
    private final Instant to = Instant.parse("2026-01-31T00:00:00Z");

    @Test
    void should_returnTopNReservationsSortedDescending_when_moreThanNExistInRange()
        throws Exception {
      User user = Fixtures.user("fede");
      Flight cheap = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Flight medium = Fixtures.flight("F2", "AeroLine", "AeroLine", 500, 1);
      Flight expensive = Fixtures.flight("F3", "AeroLine", "AeroLine", 1000, 1);
      Reservation r1 = Fixtures.reservation(1L, from, cheap, user);
      Reservation r2 = Fixtures.reservation(2L, from, medium, user);
      Reservation r3 = Fixtures.reservation(3L, from, expensive, user);
      when(reservationDao.findByDateRange(from, to)).thenReturn(List.of(r1, r2, r3));

      List<Reservation> result =
          reservationService.getTopNMostExpensive(reservationDao, from, to, 2);

      assertEquals(List.of(r3, r2), result);
    }

    @Test
    void should_returnAllReservations_when_fewerThanNExistInRange() throws Exception {
      User user = Fixtures.user("fede");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation r1 = Fixtures.reservation(1L, from, flight, user);
      when(reservationDao.findByDateRange(from, to)).thenReturn(List.of(r1));

      List<Reservation> result =
          reservationService.getTopNMostExpensive(reservationDao, from, to, 5);

      assertEquals(List.of(r1), result);
    }

    @Test
    void should_returnEmptyListWithoutCallingDao_when_nIsZero() throws Exception {
      List<Reservation> result =
          reservationService.getTopNMostExpensive(reservationDao, from, to, 0);

      assertTrue(result.isEmpty());
      verifyNoInteractions(reservationDao);
    }

    @Test
    void should_returnEmptyListWithoutCallingDao_when_nIsNegative() throws Exception {
      List<Reservation> result =
          reservationService.getTopNMostExpensive(reservationDao, from, to, -1);

      assertTrue(result.isEmpty());
      verifyNoInteractions(reservationDao);
    }

    @Test
    void should_throwInvalidDateRangeException_when_fromIsNull() {
      assertThrows(
          InvalidDateRangeException.class,
          () -> reservationService.getTopNMostExpensive(reservationDao, null, to, 3));
    }

    @Test
    void should_throwInvalidDateRangeException_when_toIsNull() {
      assertThrows(
          InvalidDateRangeException.class,
          () -> reservationService.getTopNMostExpensive(reservationDao, from, null, 3));
    }

    @Test
    void should_throwInvalidDateRangeException_when_fromIsAfterTo() {
      assertThrows(
          InvalidDateRangeException.class,
          () -> reservationService.getTopNMostExpensive(reservationDao, to, from, 3));
    }

    @Test
    void should_propagateDataAccessException_when_daoFails() throws Exception {
      when(reservationDao.findByDateRange(from, to)).thenThrow(new DataAccessException("boom"));

      assertThrows(
          DataAccessException.class,
          () -> reservationService.getTopNMostExpensive(reservationDao, from, to, 3));
    }
  }

  @Nested
  class GroupReservationsByCountryTests {

    @Test
    void should_groupByCountryAndSortByDateAscending_when_reservationsHaveLocations() {
      User user = Fixtures.user("fede");
      var argentina = Fixtures.location("Argentina", "Cordoba");
      Flight flight1 = Fixtures.flightWithArrival("F1", "AeroLine", "AeroLine", 100, 1, argentina);
      Flight flight2 = Fixtures.flightWithArrival("F2", "AeroLine", "AeroLine", 100, 1, argentina);
      Instant older = Instant.parse("2026-01-01T00:00:00Z");
      Instant newer = Instant.parse("2026-02-01T00:00:00Z");
      Reservation newerReservation = Fixtures.reservation(1L, newer, flight1, user);
      Reservation olderReservation = Fixtures.reservation(2L, older, flight2, user);

      Map<String, List<Reservation>> result =
          reservationService.groupReservationsByCountry(
              List.of(newerReservation, olderReservation));

      assertEquals(List.of(olderReservation, newerReservation), result.get("Argentina"));
    }

    @Test
    void should_ignoreReservation_when_serviceLocationIsNull() {
      User user = Fixtures.user("fede");
      Flight flightWithoutArrival = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation reservation = Fixtures.reservation(1L, Instant.now(), flightWithoutArrival, user);

      Map<String, List<Reservation>> result =
          reservationService.groupReservationsByCountry(List.of(reservation));

      assertTrue(result.isEmpty());
    }

    @Test
    void should_returnEmptyMap_when_reservationsListIsEmpty() {
      Map<String, List<Reservation>> result =
          reservationService.groupReservationsByCountry(List.of());

      assertTrue(result.isEmpty());
    }

    @Test
    void should_useAccommodationLocation_when_serviceIsAccommodation() {
      User user = Fixtures.user("fede");
      var brazil = Fixtures.location("Brazil", "Rio");
      var accommodation = Fixtures.accommodation("HotelCo", 100, 2, 3, brazil);
      Reservation reservation = Fixtures.reservation(1L, Instant.now(), accommodation, user);

      Map<String, List<Reservation>> result =
          reservationService.groupReservationsByCountry(List.of(reservation));

      assertEquals(List.of(reservation), result.get("Brazil"));
    }
  }

  @Nested
  class CreateReservationTests {

    private final String username = "fede";
    private final String flightNumber = "ABC123";

    @Test
    void should_createAndPersistReservation_when_dataIsValid() throws Exception {
      User user = Fixtures.user(username);
      Flight flight = Fixtures.flight(flightNumber, "AeroLine", "AeroLine", 100, 1);
      Instant future = Instant.now().plus(1, ChronoUnit.DAYS);
      when(userDao.findByUsername(username)).thenReturn(Optional.of(user));
      when(flightDao.findByNumber(flightNumber)).thenReturn(Optional.of(flight));

      Reservation result =
          reservationService.createReservation(
              username, flightNumber, future, userDao, flightDao, reservationDao);

      assertEquals(user, result.getOwner());
      assertEquals(flight, result.getService());
      assertEquals(future, result.getDate());
      verify(reservationDao).save(result);
    }

    @Test
    void should_throwUserNotFoundException_when_userDoesNotExist() throws Exception {
      when(userDao.findByUsername(username)).thenReturn(Optional.empty());

      assertThrows(
          UserNotFoundException.class,
          () ->
              reservationService.createReservation(
                  username,
                  flightNumber,
                  Instant.now().plusSeconds(60),
                  userDao,
                  flightDao,
                  reservationDao));
    }

    @Test
    void should_throwFlightNotFoundException_when_flightDoesNotExist() throws Exception {
      when(userDao.findByUsername(username)).thenReturn(Optional.of(Fixtures.user(username)));
      when(flightDao.findByNumber(flightNumber)).thenReturn(Optional.empty());

      assertThrows(
          FlightNotFoundException.class,
          () ->
              reservationService.createReservation(
                  username,
                  flightNumber,
                  Instant.now().plusSeconds(60),
                  userDao,
                  flightDao,
                  reservationDao));
    }

    @Test
    void should_throwInvalidDateRangeException_when_dateIsNull() throws Exception {
      when(userDao.findByUsername(username)).thenReturn(Optional.of(Fixtures.user(username)));
      when(flightDao.findByNumber(flightNumber))
          .thenReturn(Optional.of(Fixtures.flight(flightNumber, "AeroLine", "AeroLine", 100, 1)));

      assertThrows(
          InvalidDateRangeException.class,
          () ->
              reservationService.createReservation(
                  username, flightNumber, null, userDao, flightDao, reservationDao));
    }

    @Test
    void should_throwInvalidDateRangeException_when_dateIsInThePast() throws Exception {
      when(userDao.findByUsername(username)).thenReturn(Optional.of(Fixtures.user(username)));
      when(flightDao.findByNumber(flightNumber))
          .thenReturn(Optional.of(Fixtures.flight(flightNumber, "AeroLine", "AeroLine", 100, 1)));

      assertThrows(
          InvalidDateRangeException.class,
          () ->
              reservationService.createReservation(
                  username,
                  flightNumber,
                  Instant.now().minusSeconds(60),
                  userDao,
                  flightDao,
                  reservationDao));
    }

    @Test
    void should_wrapDataAccessException_when_saveFails() throws Exception {
      when(userDao.findByUsername(username)).thenReturn(Optional.of(Fixtures.user(username)));
      when(flightDao.findByNumber(flightNumber))
          .thenReturn(Optional.of(Fixtures.flight(flightNumber, "AeroLine", "AeroLine", 100, 1)));
      org.mockito.Mockito.doThrow(new DataAccessException("disk full"))
          .when(reservationDao)
          .save(any());

      DataAccessException thrown =
          assertThrows(
              DataAccessException.class,
              () ->
                  reservationService.createReservation(
                      username,
                      flightNumber,
                      Instant.now().plusSeconds(60),
                      userDao,
                      flightDao,
                      reservationDao));

      assertTrue(thrown.getMessage().contains(username));
      assertTrue(thrown.getMessage().contains(flightNumber));
    }
  }

  @Nested
  class AddReservationIfNotDuplicateTests {

    @Test
    void should_persistAndReturnReservation_when_noDuplicateExists() throws Exception {
      User user = Fixtures.user("fede");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation newReservation = Fixtures.reservation(1L, Instant.now(), flight, user);

      Reservation result =
          reservationService.addReservationIfNotDuplicate(
              List.of(), newReservation, reservationDao);

      assertEquals(newReservation, result);
      verify(reservationDao).save(newReservation);
    }

    @Test
    void should_throwDuplicateReservationException_when_duplicateExists() {
      User user = Fixtures.user("fede");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation existing = Fixtures.reservation(1L, Instant.now(), flight, user);
      Reservation duplicate = Fixtures.reservation(2L, Instant.now(), flight, user);

      assertThrows(
          DuplicateReservationException.class,
          () ->
              reservationService.addReservationIfNotDuplicate(
                  List.of(existing), duplicate, reservationDao));
    }

    @Test
    void should_notCallSave_when_duplicateExists() {
      User user = Fixtures.user("fede");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation existing = Fixtures.reservation(1L, Instant.now(), flight, user);
      Reservation duplicate = Fixtures.reservation(2L, Instant.now(), flight, user);

      assertThrows(
          DuplicateReservationException.class,
          () ->
              reservationService.addReservationIfNotDuplicate(
                  List.of(existing), duplicate, reservationDao));

      verifyNoInteractions(reservationDao);
    }
  }

  @Nested
  class ExportReservationsToCsvTests {

    @Test
    void should_delegateToExportDao_when_called() throws Exception {
      List<Reservation> reservations = List.of();

      reservationService.exportReservationsToCsv(reservations, exportDao);

      verify(exportDao).exportAll(reservations);
    }
  }

  @Nested
  class GetUserReservationSummariesTests {

    @Test
    void should_computeCorrectAggregates_when_singleUserHasMultipleReservations() throws Exception {
      User user = Fixtures.user("fede");
      Flight cheap = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Flight expensive = Fixtures.flight("F2", "AeroLine", "AeroLine", 1000, 1);
      Instant older = Instant.parse("2026-01-01T00:00:00Z");
      Instant newer = Instant.parse("2026-02-01T00:00:00Z");
      Reservation r1 = Fixtures.reservation(1L, older, cheap, user);
      Reservation r2 = Fixtures.reservation(2L, newer, expensive, user);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));

      List<UserSummaryDTO> result =
          reservationService.getUserReservationSummaries(List.of(r1, r2), userDao);

      UserSummaryDTO summary = result.get(0);
      assertEquals("fede", summary.username());
      assertEquals(2, summary.reservationCount());
      assertEquals(r1.calculatePrice() + r2.calculatePrice(), summary.totalSpent());
      assertEquals(expensive, summary.mostExpensiveService());
      assertEquals(newer, summary.mostRecentReservationDate());
    }

    @Test
    void should_returnSummariesSortedByTotalSpentDescending_when_multipleUsersHaveReservations()
        throws Exception {
      User fede = Fixtures.user("fede");
      User ana = Fixtures.user("ana");
      Flight cheap = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Flight expensive = Fixtures.flight("F2", "AeroLine", "AeroLine", 1000, 1);
      Reservation fedeReservation = Fixtures.reservation(1L, Instant.now(), cheap, fede);
      Reservation anaReservation = Fixtures.reservation(2L, Instant.now(), expensive, ana);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(fede));
      when(userDao.findByUsername("ana")).thenReturn(Optional.of(ana));

      List<UserSummaryDTO> result =
          reservationService.getUserReservationSummaries(
              List.of(fedeReservation, anaReservation), userDao);

      assertEquals("ana", result.get(0).username());
      assertEquals("fede", result.get(1).username());
    }

    @Test
    void should_throwUserNotFoundException_when_userDoesNotExist() throws Exception {
      User ghost = Fixtures.user("ghost");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation reservation = Fixtures.reservation(1L, Instant.now(), flight, ghost);
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

      assertThrows(
          UserNotFoundException.class,
          () -> reservationService.getUserReservationSummaries(List.of(reservation), userDao));
    }

    @Test
    void should_propagateDataAccessException_when_userDaoFails() throws Exception {
      User user = Fixtures.user("fede");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      Reservation reservation = Fixtures.reservation(1L, Instant.now(), flight, user);
      when(userDao.findByUsername("fede")).thenThrow(new DataAccessException("boom"));

      assertThrows(
          DataAccessException.class,
          () -> reservationService.getUserReservationSummaries(List.of(reservation), userDao));
    }
  }

  @Nested
  class ValidateReservationCandidatesTests {

    @Test
    void should_returnCandidate_when_userAndFlightExist() throws Exception {
      User user = Fixtures.user("fede");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));
      when(flightDao.findByNumber("F1")).thenReturn(Optional.of(flight));

      ValidationResultDTO result =
          reservationService.validateReservationCandidates(
              List.of(new Pair<>("fede", "F1")), userDao, flightDao);

      assertEquals(List.of(new ReservationCandidateDTO(user, flight)), result.validCandidates());
      assertTrue(result.errors().isEmpty());
    }

    @Test
    void should_addUserNotFoundError_when_userDoesNotExist() throws Exception {
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());
      when(flightDao.findByNumber("F1")).thenReturn(Optional.of(flight));

      ValidationResultDTO result =
          reservationService.validateReservationCandidates(
              List.of(new Pair<>("ghost", "F1")), userDao, flightDao);

      assertTrue(result.validCandidates().isEmpty());
      assertEquals(List.of("User not found: ghost"), result.errors());
    }

    @Test
    void should_addFlightNotFoundError_when_flightDoesNotExist() throws Exception {
      User user = Fixtures.user("fede");
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));
      when(flightDao.findByNumber("ghostFlight")).thenReturn(Optional.empty());

      ValidationResultDTO result =
          reservationService.validateReservationCandidates(
              List.of(new Pair<>("fede", "ghostFlight")), userDao, flightDao);

      assertTrue(result.validCandidates().isEmpty());
      assertEquals(List.of("Flight not found: ghostFlight"), result.errors());
    }

    @Test
    void should_addBothErrors_when_userAndFlightDoNotExist() throws Exception {
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());
      when(flightDao.findByNumber("ghostFlight")).thenReturn(Optional.empty());

      ValidationResultDTO result =
          reservationService.validateReservationCandidates(
              List.of(new Pair<>("ghost", "ghostFlight")), userDao, flightDao);

      assertTrue(result.validCandidates().isEmpty());
      assertEquals(
          List.of("User not found: ghost", "Flight not found: ghostFlight"), result.errors());
    }

    @Test
    void should_accumulateErrorsAcrossMultiplePairs_when_someAreInvalid() throws Exception {
      User fede = Fixtures.user("fede");
      Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(fede));
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());
      when(flightDao.findByNumber("F1")).thenReturn(Optional.of(flight));

      ValidationResultDTO result =
          reservationService.validateReservationCandidates(
              List.of(new Pair<>("fede", "F1"), new Pair<>("ghost", "F1")), userDao, flightDao);

      assertEquals(1, result.validCandidates().size());
      assertEquals(List.of("User not found: ghost"), result.errors());
    }

    @Test
    void should_propagateDataAccessException_when_userDaoFails() throws Exception {
      when(userDao.findByUsername("fede")).thenThrow(new DataAccessException("boom"));

      assertThrows(
          DataAccessException.class,
          () ->
              reservationService.validateReservationCandidates(
                  List.of(new Pair<>("fede", "F1")), userDao, flightDao));
    }
  }
}
