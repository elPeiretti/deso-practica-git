package service;

import domain.User;
import dto.user.UserResponse;
import exception.DataAccessException;
import exception.IncompleteUserException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.dao.FileUserDao;
import repository.dao.UserDao;
import support.Fixtures;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserDao userDao;
  @Mock private FileUserDao fileUserDao;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(userDao);
  }

  @Nested
  class GetByUsernameTests {

    @Test
    void should_returnUserResponse_when_userExists() throws Exception {
      User user = Fixtures.user("fede");
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));

      UserResponse response = userService.getByUsername("fede");

      assertEquals(user.getId(), response.id());
      assertEquals("fede", response.username());
    }

    @Test
    void should_throwUserNotFoundException_when_userDoesNotExist() throws Exception {
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

      assertThrows(UserNotFoundException.class, () -> userService.getByUsername("ghost"));
    }
  }

  @Nested
  class GetValidatedUserTests {

    @Test
    void should_returnUser_when_profileIsComplete() throws Exception {
      var location = Fixtures.location("Argentina", "Cordoba");
      User user = Fixtures.user("fede", location);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));

      User result = userService.getValidatedUser("fede", userDao);

      assertEquals(user, result);
    }

    @Test
    void should_throwUserNotFoundException_when_userDoesNotExist() throws Exception {
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

      assertThrows(
          UserNotFoundException.class, () -> userService.getValidatedUser("ghost", userDao));
    }

    @Test
    void should_throwIncompleteUserException_when_addressIsNull() throws Exception {
      User user = Fixtures.user("fede", null);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));

      assertThrows(
          IncompleteUserException.class, () -> userService.getValidatedUser("fede", userDao));
    }

    @Test
    void should_throwIncompleteUserException_when_countryIsBlank() throws Exception {
      var location = Fixtures.location("   ", "Cordoba");
      User user = Fixtures.user("fede", location);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));

      assertThrows(
          IncompleteUserException.class, () -> userService.getValidatedUser("fede", userDao));
    }

    @Test
    void should_throwIncompleteUserException_when_cityIsEmpty() throws Exception {
      var location = Fixtures.location("Argentina", "");
      User user = Fixtures.user("fede", location);
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(user));

      assertThrows(
          IncompleteUserException.class, () -> userService.getValidatedUser("fede", userDao));
    }

    @Test
    void should_propagateDataAccessException_when_daoFails() throws Exception {
      when(userDao.findByUsername("fede")).thenThrow(new DataAccessException("boom"));

      assertThrows(DataAccessException.class, () -> userService.getValidatedUser("fede", userDao));
    }
  }

  @Nested
  class GetUsersPresentInFileAndDaoTests {

    @Test
    void should_returnOnlyUsersPresentInBoth_when_someFileUsersExistInDao() throws Exception {
      User fileFede = Fixtures.user("fede");
      User fileAna = Fixtures.user("ana");
      User daoFede = Fixtures.user("fede");
      when(fileUserDao.findAll()).thenReturn(List.of(fileFede, fileAna));
      when(userDao.findByUsername("fede")).thenReturn(Optional.of(daoFede));
      when(userDao.findByUsername("ana")).thenReturn(Optional.empty());

      List<User> result = userService.getUsersPresentInFileAndDao(fileUserDao, userDao);

      assertEquals(List.of(daoFede), result);
    }

    @Test
    void should_returnEmptyList_when_noFileUsersExistInDao() throws Exception {
      when(fileUserDao.findAll()).thenReturn(List.of(Fixtures.user("ghost")));
      when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

      List<User> result = userService.getUsersPresentInFileAndDao(fileUserDao, userDao);

      assertTrue(result.isEmpty());
    }

    @Test
    void should_ignoreUser_when_userDaoThrowsDataAccessExceptionForThatUser() throws Exception {
      when(fileUserDao.findAll()).thenReturn(List.of(Fixtures.user("fede"), Fixtures.user("ana")));
      when(userDao.findByUsername("fede")).thenThrow(new DataAccessException("boom"));
      when(userDao.findByUsername("ana")).thenReturn(Optional.of(Fixtures.user("ana")));

      List<User> result = userService.getUsersPresentInFileAndDao(fileUserDao, userDao);

      assertEquals(1, result.size());
      assertEquals("ana", result.get(0).getUsername());
    }

    @Test
    void should_propagateDataAccessException_when_fileUserDaoFails() throws Exception {
      when(fileUserDao.findAll()).thenThrow(new DataAccessException("boom"));

      assertThrows(
          DataAccessException.class,
          () -> userService.getUsersPresentInFileAndDao(fileUserDao, userDao));
    }

    @Test
    void should_returnEmptyList_when_fileHasNoUsers() throws Exception {
      when(fileUserDao.findAll()).thenReturn(List.of());

      List<User> result = userService.getUsersPresentInFileAndDao(fileUserDao, userDao);

      assertTrue(result.isEmpty());
    }
  }
}
