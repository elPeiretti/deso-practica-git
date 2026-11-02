package repository.dao;

import domain.Reservation;
import exception.DataAccessException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReservationDaoFileImpl implements ReservationDao {
  private static ReservationDaoFileImpl instance;
  private final File file;

  private ReservationDaoFileImpl() {
    this.file = new File("reservations.csv");
  }

  // visible for testing, so tests can point at a temp file instead of the real one
  ReservationDaoFileImpl(File file) {
    this.file = file;
  }

  // singleton
  public static synchronized ReservationDaoFileImpl getInstance() {
    if (instance == null) {
      instance = new ReservationDaoFileImpl();
    }

    return instance;
  }

  @Override
  public void save(Reservation reservation) throws DataAccessException {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
      String line =
          String.format(
              "%s,%s,%s,%s\n",
              reservation.getId(),
              reservation.getService().getId(),
              reservation.getOwner().getId(),
              reservation.getDate());
      bw.write(line);
    } catch (IOException e) {
      throw new DataAccessException("Error al guardar la reserva", e);
    }
  }
//--------------------------------------------------------------------------------------------------------------
  // Ejercicio 3 (Parte A)
 // REALIZADO PORGIOVANI MICHELOTTI------CLASE:RESERVATIONDAOFILEIMPL---
  
 @Override
public List<Reservation> findByDateRange(Instant from, Instant to) throws DataAccessException {
    try {
        return this.findAll().stream()
                .filter(reservation -> reservation != null && reservation.getDate() != null)
                .filter(reservation -> from == null || !reservation.getDate().isBefore(from))
                .filter(reservation -> to == null || !reservation.getDate().isAfter(to))
                .toList();
    } catch (Exception e) {
        throw new DataAccessException("Error al filtrar reservas por rango de fechas", e);
    }
}
  //METODO AGREGADO(ABAJO) PARA PODER HACER EL FILTRADO Y TAMBIEN AGREGUE EN LA CLASE 
  // RESERVATION.JAVA UN METODO AUXILIAR EQUALS 
  //POR QUE SINO NO TENES FORMA DE VER SI 2 RESERVAS SON IGUALES RESPECTO SU ID,SOLO PODIAS VERLO DESDE SU ESPACIO
  //EN MEMORIA AL CUAL SU DIRECCION ES UNICA  

 private List<Reservation> findAll() throws Exception {
    if (this.file == null || !this.file.exists() || this.file.length() == 0) { 
      return Collections.emptyList();
    }
    
    List<Reservation> reservations = new ArrayList<>();
    
    try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {
      String line;
      while ((line = br.readLine()) != null) {//al ser un archivo.txt lo tengo que leer linea por linea
        if (!line.trim().isEmpty()) {
          String[] parts = line.split(",");
          
          if (parts.length >= 4) {
            //Creo objetos anónimos simulados para satisfacer al Builder,si ven otra solucion mas viable cambiar
            domain.TouristService mockService = new domain.TouristService(line) {
              @Override
              public double calculatePrice() { return 0.0; }
              @Override
              public String getId() { return parts[1].trim(); }
            };
            
            domain.User mockUser = new domain.User() {
              @Override
              public String getId() { return parts[2].trim(); }
            };
            
            // Reconstruyo la Reserva usando su Builder con los datos del CSV
            Reservation reservation = new Reservation.Builder()
                .id(Long.parseLong(parts[0].trim()))
                .service(mockService) //un objeto "fantasma" de tipo TouristService,no esta cargado en la base de dstos es para que builder me deje usar el metodo y funcione
                .owner(mockUser)//un objeto "fantasma" de tipo User. Solo le asignamos el ID del dueño (parts[2]),para que builder me deje usarlo
                .date(Instant.parse(parts[3].trim()))
                .build();
            
            reservations.add(reservation);
          }
        }
      }
    }
    return reservations;
  } 
}
