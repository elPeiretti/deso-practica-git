![banner de github](img/Banner-3.jpg)

# Desarrollo de Software - Ejercicios integradores

Proyecto de práctica sobre un sistema de reservas turísticas (vuelos, alojamientos,
usuarios). La lógica de negocio se resuelve **en la capa de servicio** (`service`) y,
en algunos ejercicios, también en la capa de acceso a datos (`repository.dao`).

Los métodos implementados en el dominio (`domain`) y las excepciones ya provistas **no deben modificarse**. Cada
método de servicio o DAO a implementar ya tiene su firma definida; no debe alterarse,
ya que de lo contrario los tests no compilarán.

Cada ejercicio se encuentra sin resolver: su método lanza `UnsupportedOperationException`.
El objetivo es implementar la lógica correspondiente hasta que los tests pasen, sin
modificar la firma de los métodos ni las clases de dominio o excepciones.

<img src="img/concepto-decorativo-codigo-binario-banner-digital-programacion-codificacion_1055-24774.avif" width=800px height=100>

### Ejercicio 1 — Filtrado y ordenamiento por precio(de mayor a menor)

```java 
   ReservationService.getReservationsByUser(List<Reservation>, String username, UserDao)
```
### Ejercicio 2 — Agrupamiento y promedio por compañía

```java
   TouristServiceService.getAveragePriceByCompany(List<TouristService>, String company)
```
### Ejercicio 3 — Top N reservas más caras en un rango de fechas

**Parte A (DAO):** rango de fechas

```java
   ReservationDaoFileImpl.findByDateRange(Instant from, Instant to)
```

**Parte B (Service):** reservas mas caras de forma descendente

```java
   ReservationService.getTopNMostExpensive(ReservationDao, Instant from, Instant to, int n)
```
### Ejercicio 4 — Ordenamiento compuesto de vuelos con enriquecimiento desde DAO

```java
   TouristServiceService.getFlightsSortedByPriceThenAirline(List<String> flightNumbers, FlightDao)
```
 Debe recuperarse cada vuelo del DAO por número y devolverse la lista ordenada por precio calculado ascendente y, en caso de empate, por aerolínea en orden alfabético

### Ejercicio 5 — Agrupamiento de reservas por país del servicio

```java
   ReservationService.groupReservationsByCountry(List<Reservation>)
```

Dentro de cada grupo, ordenarlas por fecha ascendente. 

### Ejercicio 6 — Búsqueda de usuario con validación de perfil completo

```java
   UserService.getValidatedUser(String username, UserDao)
```

Debe buscarse al usuario y validarse que posea `address` con `country` y `city` no
nulos ni vacíos/en blanco. 

### Ejercicio 7 — Creación de reserva con validación completa y manejo de fallo en persistencia

```java
   ReservationService.createReservation(String username, String flightNumber, Instant date, UserDao, FlightDao, ReservationDao)
```

construir la reserva con
`Reservation.Builder` y persistirla. Si `save()` lanza `DataAccessException`, esta no
debe propagarse tal cual: debe envolverse en una nueva `DataAccessException` cuyo
mensaje incluya el username y el flightNumber.

### Ejercicio 8 — Detección de duplicados antes de persistir

```java
   ReservationService.addReservationIfNotDuplicate(List<Reservation> existing, Reservation newReservation, ReservationDao)
```

Si ya existe una reserva con el mismo `user.getId()` y `service.getId()`, debe
lanzarse `DuplicateReservationException`.

### Ejercicio 9 — Lectura de archivo y cruce con DAO

**Parte A (DAO):**

```java
   FileUserDaoImpl.findAll()
```

debe leer `users.txt` línea por línea
(formato `uuid username`), ignorando las líneas mal formadas, y envolver cualquier
error de lectura en `DataAccessException`.

**Parte B (Service):**

```java
   UserService.getUsersPresentInFileAndDao(FileUserDao, UserDao)
```

Debe devolverse únicamente a los usuarios presentes en el archivo **y** existentes en
el `UserDao`. Si el `UserDao` falla para un usuario puntual, este debe ignorarse y
continuar con el resto. Si `findAll()` falla, la excepción debe propagarse.

### Ejercicio 10 — Exportación de reservas a CSV

**Parte A (DAO):**

```java
   ReservationExportDaoImpl.exportAll(List<Reservation>)
```

debe
escribir un CSV con encabezado `reservationId,date,ownerUsername,serviceCompany,calculatedPrice`,
el precio con dos decimales, la fecha en formato ISO-8601, y únicamente el encabezado
cuando la lista está vacía. Cualquier `IOException` debe envolverse en
`DataAccessException`.

**Parte B (Service):**

```java
   ReservationService.exportReservationsToCsv(List<Reservation>, ReservationExportDao)
```

debe delegar en el DAO y propagar cualquier `DataAccessException`.

### Ejercicio 11 — Resumen por usuario con estadísticas de reservas

```java
   ReservationService.getUserReservationSummaries(List<Reservation>, UserDao)
```

Debe construirse un `UserSummaryDTO` por cada usuario presente en las reservas
(cantidad de reservas, total gastado, servicio más caro y fecha de reserva más
reciente), utilizando streams. Si un usuario no existe en el `UserDao`, debe
lanzarse `UserNotFoundException`. La lista resultante debe devolverse ordenada por
`totalSpent` de forma descendente.

### Ejercicio 12 — Validación por lotes con acumulación de errores

```java
   ReservationService.validateReservationCandidates(List<Pair<String, String>>, UserDao, FlightDao)
```

Para cada par `(username, flightNumber)`, si ambos existen debe agregarse un
`ReservationCandidateDTO`; caso contrario, debe acumularse un mensaje de error por
cada dato faltante (`"User not found: <username>"`, `"Flight not found: <flightNumber>"`)
sin detener el proceso. Si el `UserDao` lanza `DataAccessException`, esta sí debe
propagarse (fallo total). Debe devolverse un `ValidationResultDTO` con los candidatos
válidos y los errores acumulados.

---