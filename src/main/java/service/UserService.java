package service;

import domain.Location;
import domain.User;
import dto.user.UserResponse;
import exception.DataAccessException;
import exception.IncompleteUserException;
import exception.UserNotFoundException;
import repository.dao.FileUserDao;
import repository.dao.UserDao;

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
    User user = userDao.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));

    Location address = user.getAddress();

    if (address == null ||
            address.getCountry() == null || address.getCountry().trim().isEmpty() ||
            address.getCity() == null || address.getCity().trim().isEmpty()) {

      throw new IncompleteUserException("El perfil del usuario está incompleto");
    }

    return user;
  }

  // Ejercicio 9
  public List<User> getUsersPresentInFileAndDao(FileUserDao fileUserDao, UserDao userDao)
      throws DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 9: implement getUsersPresentInFileAndDao");
  }
}
