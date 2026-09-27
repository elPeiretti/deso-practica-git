package exception;

public class DuplicateReservationException extends Exception {
  public DuplicateReservationException(String userId, String serviceId) {
    super("Duplicate reservation detected for user " + userId + " and service " + serviceId);
  }
}
