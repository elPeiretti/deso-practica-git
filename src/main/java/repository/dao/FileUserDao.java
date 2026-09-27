package repository.dao;

import domain.User;
import exception.DataAccessException;

import java.util.List;

public interface FileUserDao {
  List<User> findAll() throws DataAccessException;
}
