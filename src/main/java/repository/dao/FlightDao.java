package repository.dao;

import domain.Flight;

import java.util.Optional;
import java.util.UUID;

public interface FlightDao {
  Optional<Flight> findByNumber(String number);
  Optional<Flight> findById(UUID id);
}
