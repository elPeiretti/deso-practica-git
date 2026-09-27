package exception;

public class ServiceNotFoundException extends Exception {
  public ServiceNotFoundException(String company) {
    super("No services found for company: " + company);
  }
}
