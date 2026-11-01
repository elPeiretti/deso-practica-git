package service;

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
// Ejercicio 6
  public User getValidatedUser(String username, UserDao userDao)
          throws UserNotFoundException, IncompleteUserException, DataAccessException {


    User user = userDao.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));


    if (user.getAddress() == null) {
      throw new IncompleteUserException(username);
    }


    String country = user.getAddress().getCountry();
    String city = user.getAddress().getCity();


    if (country == null || country.isBlank()
            || city == null || city.isBlank()) {
      throw new IncompleteUserException(username);
    }


    return user;
  }


  // Ejercicio 9
  public List<User> getUsersPresentInFileAndDao(FileUserDao fileUserDao, UserDao userDao)
      throws DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 9: implement getUsersPresentInFileAndDao");
  }
}
