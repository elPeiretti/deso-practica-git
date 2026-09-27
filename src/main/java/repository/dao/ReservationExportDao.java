package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.util.List;

public interface ReservationExportDao {
  void exportAll(List<Reservation> reservations) throws DataAccessException;
}
