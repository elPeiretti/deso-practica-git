package service;
import domain.Location;
import domain.User;
import dto.user.UserResponse;
import exception.DataAccessException;
import exception.IncompleteUserException;
import exception.UserNotFoundException;
import repository.dao.FileUserDao;
import repository.dao.UserDao;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class UserService {
  private final UserDao userDao;

  public UserService(UserDao userDao) {
    this.userDao = userDao;
  }

  public UserResponse getByUsername(String username)
      throws UserNotFoundException, DataAccessException {
    User user =
        userDao.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    return new UserResponse(user.getId(), user.getUsername());
  }

  // Ejercicio 6
  public User getValidatedUser(String username, UserDao userDao)
      throws UserNotFoundException, IncompleteUserException, DataAccessException {
    User user =
        userDao.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

    Location address = user.getAddress();
    if (address == null || isBlank(address.getCountry()) || isBlank(address.getCity())) {
      throw new IncompleteUserException(username);
    }

    return user;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  // Ejercicio 9 (Parte B)
public List<User> getUsersPresentInFileAndDao(FileUserDao fileUserDao, UserDao userDao) throws DataAccessException {
    List<User> listaUsuaio = fileUserDao.findAll(); //propaga la excepcion
    List<User> result = new ArrayList<>();
    if (listaUsuaio == null || listaUsuaio.isEmpty()) {
        return result;
    }
    for (User fileUser : listaUsuaio) {
        try {
            Optional<User> dbUser = userDao.findByUsername(fileUser.getUsername());
            if (dbUser.isPresent()) { result.add(dbUser.get()); }
        } catch (DataAccessException e) {
            // Se ignora el usuario puntual si falla la consulta en UserDao
        }
    }

    return result;
}
}
