package service;

import domain.User;
import dto.user.UserResponse;
import exception.DataAccessException;
import exception.IncompleteUserException;
import exception.UserNotFoundException;
import repository.dao.FileUserDao;
import repository.dao.UserDao;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

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
    User user = userDao.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

    if(user.getAddress() != null &&
            user.getAddress().getCountry() != null && !user.getAddress().getCountry().isBlank() &&
            user.getAddress().getCity() != null && !user.getAddress().getCity().isBlank()){
      return user;
    }
    else throw new IncompleteUserException(username);
  }

  // Ejercicio 9
  public List<User> getUsersPresentInFileAndDao(FileUserDao fileUserDao, UserDao userDao)
          throws DataAccessException {
    List<User> fromFile = fileUserDao.findAll(); // si falla, se propaga

    List<User> result = new ArrayList<>();
    for (User fileUser : fromFile) {
      try {
        userDao.findByUsername(fileUser.getUsername()).ifPresent(result::add);
      } catch (DataAccessException e) {
      }
    }
    return result;
  }
}
