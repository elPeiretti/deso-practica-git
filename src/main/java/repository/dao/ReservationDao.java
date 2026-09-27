package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.time.Instant;
import java.util.List;

public interface ReservationDao {
  void save(Reservation reservation) throws DataAccessException;

  List<Reservation> findByDateRange(Instant from, Instant to) throws DataAccessException;
}
