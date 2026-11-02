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
User aux= userDao.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
// Validar que el usuario tenga direccion
Location address=aux.getAddress();

// 2. Validar que la dirección exista y que city/country no sean nulos ni estén en blanco
    if (address == null || 
        address.getCity() == null || address.getCity().isBlank() || 
        address.getCountry() == null || address.getCountry().isBlank()) {
        throw new IncompleteUserException(username);
    }
    
    // 3. Retornar el usuario validado
    return aux;
}

  // Ejercicio 9
  public List<User> getUsersPresentInFileAndDao(FileUserDao fileUserDao, UserDao userDao)
      throws DataAccessException {
    throw new UnsupportedOperationException("Ejercicio 9: implement getUsersPresentInFileAndDao");
  }
}
