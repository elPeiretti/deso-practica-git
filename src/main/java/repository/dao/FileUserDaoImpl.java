package repository.dao;

import domain.User;
import exception.DataAccessException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    List<User> users = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        parseLine(line).ifPresent(users::add);
      }
    } catch (IOException e) {
      throw new DataAccessException("Error al leer el archivo de usuarios", e);
    }

    return users;
  }

  private Optional<User> parseLine(String line) {
    String[] data = line.split(" ");
    if (data.length != 2) {
      return Optional.empty();
    }

    try {
      return Optional.of(User.builder().id(UUID.fromString(data[0])).username(data[1]).build());
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
