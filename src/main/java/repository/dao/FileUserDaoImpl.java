package repository.dao;

import domain.User;
import exception.DataAccessException;

import java.io.File;
import java.util.List;

public class FileUserDaoImpl implements FileUserDao {
  private final File file;

  public FileUserDaoImpl() {
    this.file = new File("users.txt");
  }

  // visible for testing, so tests can point at a temp file instead of the real one
  FileUserDaoImpl(File file) {
    this.file = file;
  }

  // Ejercicio 9 (Parte A)
  @Override
  public List<User> findAll() throws DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 9: implement findAll");
  }
}
