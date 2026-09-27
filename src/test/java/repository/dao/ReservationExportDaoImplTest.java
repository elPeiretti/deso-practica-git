package repository.dao;

import domain.Flight;
import domain.Reservation;
import domain.User;
import exception.DataAccessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import support.Fixtures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationExportDaoImplTest {

  @TempDir File tempDir;

  @Test
  void should_writeHeaderAndRow_when_reservationsProvided() throws Exception {
    File file = new File(tempDir, "export.csv");
    User user = Fixtures.user("fede");
    Flight flight = Fixtures.flight("F1", "AeroLine SA", "AeroLine SA", 100, 1);
    Instant date = Instant.parse("2026-08-23T22:25:31.973Z");
    Reservation reservation = Fixtures.reservation(971L, date, flight, user);

    new ReservationExportDaoImpl(file).exportAll(List.of(reservation));

    List<String> lines = readLines(file);
    assertEquals("reservationId,date,ownerUsername,serviceCompany,calculatedPrice", lines.get(0));
    assertEquals(
        String.format("971,%s,fede,AeroLine SA,%.2f", date, reservation.calculatePrice()),
        lines.get(1));
  }

  @Test
  void should_writeOnlyHeader_when_reservationsListIsEmpty() throws Exception {
    File file = new File(tempDir, "export.csv");

    new ReservationExportDaoImpl(file).exportAll(List.of());

    List<String> lines = readLines(file);
    assertEquals(1, lines.size());
    assertEquals("reservationId,date,ownerUsername,serviceCompany,calculatedPrice", lines.get(0));
  }

  @Test
  void should_formatPriceWithTwoDecimals_when_exporting() throws Exception {
    File file = new File(tempDir, "export.csv");
    User user = Fixtures.user("fede");
    Flight flight = Fixtures.flight("F1", "AeroLine SA", "AeroLine SA", 1, 1);
    Reservation reservation = Fixtures.reservation(1L, Instant.now(), flight, user);

    new ReservationExportDaoImpl(file).exportAll(List.of(reservation));

    List<String> lines = readLines(file);
    assertTrue(lines.get(1).matches(".*,\\d+\\.\\d{2}$"));
  }

  @Test
  void should_throwDataAccessException_when_fileCannotBeWritten() {
    File directoryAsFile = tempDir;

    assertThrows(
        DataAccessException.class,
        () -> new ReservationExportDaoImpl(directoryAsFile).exportAll(List.of()));
  }

  private List<String> readLines(File file) throws IOException {
    return Files.readAllLines(file.toPath());
  }
}
