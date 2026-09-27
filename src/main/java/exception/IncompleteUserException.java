package exception;

public class IncompleteUserException extends Exception {
  public IncompleteUserException(String username) {
    super("User " + username + " has incomplete profile data");
  }
}
