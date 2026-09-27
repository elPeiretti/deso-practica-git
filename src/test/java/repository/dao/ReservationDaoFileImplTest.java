package repository.dao;

import domain.Flight;
import domain.Reservation;
import domain.User;
import exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import support.Fixtures;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationDaoFileImplTest {

  @TempDir File tempDir;

  private ReservationDaoFileImpl reservationDao;

  private final Instant from = Instant.parse("2026-01-01T00:00:00Z");
  private final Instant to = Instant.parse("2026-01-31T00:00:00Z");

  @BeforeEach
  void setUp() {
    reservationDao = new ReservationDaoFileImpl(new File(tempDir, "reservations.csv"));
  }

  @Test
  void should_returnEmptyList_when_noReservationsSaved() throws DataAccessException {
    assertTrue(reservationDao.findByDateRange(from, to).isEmpty());
  }

  @Test
  void should_includeReservationAtLowerBound_when_dateEqualsFrom() throws DataAccessException {
    Reservation reservation = savedReservation(1L, from);

    List<Reservation> result = reservationDao.findByDateRange(from, to);

    assertEquals(List.of(reservation), result);
  }

  @Test
  void should_includeReservationAtUpperBound_when_dateEqualsTo() throws DataAccessException {
    Reservation reservation = savedReservation(1L, to);

    List<Reservation> result = reservationDao.findByDateRange(from, to);

    assertEquals(List.of(reservation), result);
  }

  @Test
  void should_excludeReservation_when_dateIsBeforeRange() throws DataAccessException {
    savedReservation(1L, from.minusSeconds(1));

    assertTrue(reservationDao.findByDateRange(from, to).isEmpty());
  }

  @Test
  void should_excludeReservation_when_dateIsAfterRange() throws DataAccessException {
    savedReservation(1L, to.plusSeconds(1));

    assertTrue(reservationDao.findByDateRange(from, to).isEmpty());
  }

  @Test
  void should_writeToFile_when_saveIsCalled() throws DataAccessException {
    savedReservation(1L, from);

    File file = new File(tempDir, "reservations.csv");
    assertTrue(file.exists());
  }

  private Reservation savedReservation(Long id, Instant date) throws DataAccessException {
    User user = Fixtures.user("fede");
    Flight flight = Fixtures.flight("F1", "AeroLine", "AeroLine", 100, 1);
    Reservation reservation = Fixtures.reservation(id, date, flight, user);
    reservationDao.save(reservation);
    return reservation;
  }
}
