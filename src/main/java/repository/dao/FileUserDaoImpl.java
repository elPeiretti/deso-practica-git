package repository.dao;

import domain.User;
import exception.DataAccessException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserDaoImpl implements FileUserDao {
  private final File file;

  public FileUserDaoImpl() {
    this.file = new File("users.txt");
  }

  FileUserDaoImpl(File file) {
    this.file = file;
  }

  @Override
  public List<User> findAll() throws DataAccessException {
    List<User> users = new ArrayList<>();

    try (BufferedReader reader =
                 Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        User user = parseLine(line);
        if (user != null) {
          users.add(user);
        }
      }
    } catch (IOException e) {
      throw new DataAccessException("Error leyendo el archivo " + file, e);
    }

    return users;
  }

  // Devuelve null si la línea está mal formada
  private User parseLine(String line) {
    String[] parts = line.trim().split("\\s+");
    if (parts.length != 2) {
      return null; // vacía o con más/menos de 2 campos
    }
    try {
      UUID id = UUID.fromString(parts[0]);
      return User.builder().id(id).username(parts[1]).build();
    } catch (IllegalArgumentException e) {
      return null; // UUID inválido
    }
  }
}
