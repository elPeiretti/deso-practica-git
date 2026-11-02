package service;

import domain.Location;
import domain.User;
import dto.user.UserResponse;
import exception.DataAccessException;
import exception.IncompleteUserException;
import exception.UserNotFoundException;
import repository.dao.FileUserDao;
import repository.dao.UserDao;

import java.util.ArrayList;
import java.util.List;

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
    if (address == null  || isNullOrBlank(address.getCountry()) || isNullOrBlank(address.getCity())) {
      throw new IncompleteUserException(username);
    }

    return user;
  }

  private boolean isNullOrBlank(String value) {
    return value == null || value.isBlank();
  }

  // Ejercicio 9
  public List<User> getUsersPresentInFileAndDao(FileUserDao fileUserDao, UserDao userDao)
          throws DataAccessException {
    List<User> usersInBoth = new ArrayList<>();

    for (User fileUser : fileUserDao.findAll()) {
      try {
        userDao.findByUsername(fileUser.getUsername()).ifPresent(usersInBoth::add);
      } catch (DataAccessException e) {
        // Un fallo puntual no corta el proceso: se ignora este usuario y se sigue con el resto
      }
    }

    return usersInBoth;
  }
}