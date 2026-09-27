package repository.dao;

import domain.User;
import exception.DataAccessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUserDaoImplTest {

  @TempDir File tempDir;

  @Test
  void should_returnAllValidUsers_when_fileHasValidLines() throws Exception {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    File file = writeFile(id1 + " fede\n" + id2 + " ana\n");

    List<User> users = new FileUserDaoImpl(file).findAll();

    assertEquals(2, users.size());
    assertEquals("fede", users.get(0).getUsername());
    assertEquals("ana", users.get(1).getUsername());
  }

  @Test
  void should_skipMalformedLines_when_lineHasWrongTokenCount() throws Exception {
    UUID id = UUID.randomUUID();
    File file = writeFile(id + " fede extraToken\n" + "onlyOneToken\n" + id + " ana\n");

    List<User> users = new FileUserDaoImpl(file).findAll();

    assertEquals(1, users.size());
    assertEquals("ana", users.get(0).getUsername());
  }

  @Test
  void should_returnEmptyList_when_fileIsEmpty() throws Exception {
    File file = writeFile("");

    List<User> users = new FileUserDaoImpl(file).findAll();

    assertTrue(users.isEmpty());
  }

  @Test
  void should_throwDataAccessException_when_fileDoesNotExist() {
    File missingFile = new File(tempDir, "does-not-exist.txt");

    assertThrows(DataAccessException.class, () -> new FileUserDaoImpl(missingFile).findAll());
  }

  private File writeFile(String content) throws IOException {
    File file = new File(tempDir, "users.txt");
    Files.writeString(file.toPath(), content);
    return file;
  }
}
