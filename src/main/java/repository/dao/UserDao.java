package repository.dao;

import domain.User;
import exception.DataAccessException;
import exception.UserNotFoundException;

import java.util.Optional;
import java.util.UUID;

public interface UserDao {
  Optional<User> findByUsername(String username) throws DataAccessException;
  Optional<User> findById(UUID id) throws DataAccessException;
}
