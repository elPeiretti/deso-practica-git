# Desarrollo de Software - Ejercicios integradores

Proyecto de práctica sobre un sistema de reservas turísticas (vuelos, alojamientos,
usuarios). La lógica de negocio se resuelve **en la capa de servicio** (`service`) y,
en algunos ejercicios, también en la capa de acceso a datos (`repository.dao`).

El dominio (`domain`) y las excepciones ya provistas **no deben modificarse**. Cada
método de servicio o DAO a implementar ya tiene su firma definida; no debe alterarse,
ya que de lo contrario los tests no compilarán.

Cada ejercicio se encuentra sin resolver: su método lanza `UnsupportedOperationException`.
El objetivo es implementar la lógica correspondiente hasta que los tests pasen, sin
modificar la firma de los métodos ni las clases de dominio o excepciones.

---

## Cómo entregar el trabajo (Pull Request)

1. Realizar un **fork** de este repositorio a la cuenta propia o a la de uno de los
   integrantes del grupo.
2. Clonar el fork e implementar los ejercicios que se hayan decidido resolver.
3. Confirmar los cambios (`commit`) y enviarlos al fork (`push`).
4. Abrir un Pull Request desde el fork hacia la rama `main` de **este** repositorio.
   GitHub completará automáticamente la descripción con la plantilla de
   `.github/PULL_REQUEST_TEMPLATE.md`.
5. Reemplazar `{}` en el título de la descripción (`# Grupo {}`) por el número de
   grupo correspondiente.
6. **Marcar con una `x` únicamente los ejercicios implementados:**

   ```markdown
   - [x] Ejercicio 1 — Filtrado y ordenamiento por precio
   - [ ] Ejercicio 2 — Agrupamiento y promedio por compañía
   ```

   No debe eliminarse el texto `Ejercicio N` de cada línea ni modificarse los
   números: el CI utiliza ese texto para determinar a qué ejercicio corresponde
   cada casillero.

7. El workflow de CI (`.github/workflows/maven-tests.yml`) realizará lo siguiente:
   - Ejecutar toda la batería de tests.
   - Revisar el checklist y exigir que **cada ejercicio marcado pase todos sus
     tests**. Un ejercicio marcado que continúe sin implementarse (o esté mal
     implementado) hará fallar el CI.
   - Ignorar por completo los ejercicios sin marcar, aunque sus tests fallen.
   - Publicar una tabla con el resultado de cada ejercicio en el resumen de la
     ejecución (pestaña *Actions* → ejecución correspondiente → *Summary*).

8. Si se envían nuevos cambios o se edita el PR para marcar más ejercicios, el CI se
   ejecutará nuevamente de forma automática.

### Importante

- No deben modificarse las firmas de los métodos ni las clases en `domain` o
  `exception`.
- No deben modificarse los archivos de test.


---

## Ejercicios

> En algunos ejercicios el método recibe listas de objetos directamente como
> parámetro; se trata de una simplificación intencional para centrar el ejercicio en
> la lógica del service. En los Ejercicios 3, 9 y 10 también debe completarse una
> implementación de DAO (Parte A) además del método de servicio (Parte B).

### Ejercicio 1 — Filtrado y ordenamiento por precio

```java
   ReservationService.getReservationsByUser(List<Reservation>, String username, UserDao)
```

Debe devolver únicamente las reservas del usuario indicado, ordenadas por precio
calculado (`reservation.calculatePrice()`) de mayor a menor. Si el usuario no existe
en el `UserDao`, debe lanzarse `UserNotFoundException`. Se debe utilizar streams y
lambdas.

### Ejercicio 2 — Agrupamiento y promedio por compañía

```java
   TouristServiceService.getAveragePriceByCompany(List<TouristService>, String company)
```

Debe devolver el precio promedio (`service.calculatePrice()`) de los servicios de una
compañía dada, con comparación case-insensitive. Si ningún servicio pertenece a esa
compañía, debe lanzarse `ServiceNotFoundException`.

### Ejercicio 3 — Top N reservas más caras en un rango de fechas

**Parte A (DAO):**

```java
   ReservationDaoFileImpl.findByDateRange(Instant from, Instant to)
```

debe devolver las reservas guardadas cuya fecha se encuentre dentro del rango
(inclusive en ambos extremos). El filtro por fecha debe realizarse **únicamente** en
el DAO.

**Parte B (Service):**

```java
   ReservationService.getTopNMostExpensive(ReservationDao, Instant from, Instant to, int n)
```

Debe validarse el rango (`InvalidDateRangeException` si `from`/`to` son `null` o si
`from` es posterior a `to`), invocar al DAO y devolver las `n` reservas más caras
ordenadas de forma descendente. Si `n <= 0`, debe devolverse una lista vacía **sin**
invocar al DAO. Si existen menos de `n` reservas, deben devolverse todas.

### Ejercicio 4 — Ordenamiento compuesto de vuelos con enriquecimiento desde DAO

```java
   TouristServiceService.getFlightsSortedByPriceThenAirline(List<String> flightNumbers, FlightDao)
```

Debe recuperarse cada vuelo del DAO por número y devolverse la lista ordenada por
precio calculado ascendente y, en caso de empate, por aerolínea en orden alfabético
(`Comparator.comparing(...).thenComparing(...)`). Si algún número no existe, debe
lanzarse `FlightNotFoundException`.

### Ejercicio 5 — Agrupamiento de reservas por país del servicio

```java
   ReservationService.groupReservationsByCountry(List<Reservation>)
```

Debe agruparse las reservas por país de la ubicación del servicio
(`Accommodation.getLocation()` o `Flight.getArrivalInfo()`, utilizando `instanceof`
con pattern matching) y, dentro de cada grupo, ordenarlas por fecha ascendente. Deben
ignorarse las reservas cuyo servicio no tenga ubicación asignada. Se debe utilizar
`Collectors.groupingBy`.

### Ejercicio 6 — Búsqueda de usuario con validación de perfil completo

```java
   UserService.getValidatedUser(String username, UserDao)
```

Debe buscarse al usuario y validarse que posea `address` con `country` y `city` no
nulos ni vacíos/en blanco. Debe lanzarse `UserNotFoundException` si no existe, o
`IncompleteUserException` si el perfil está incompleto.

### Ejercicio 7 — Creación de reserva con validación completa y manejo de fallo en persistencia

```java
   ReservationService.createReservation(String username, String flightNumber, Instant date, UserDao, FlightDao, ReservationDao)
```

Deben validarse el usuario, el vuelo y que la fecha no sea nula ni anterior al
momento actual (`InvalidDateRangeException`), construir la reserva con
`Reservation.Builder` y persistirla. Si `save()` lanza `DataAccessException`, esta no
debe propagarse tal cual: debe envolverse en una nueva `DataAccessException` cuyo
mensaje incluya el username y el flightNumber.

### Ejercicio 8 — Detección de duplicados antes de persistir

```java
   ReservationService.addReservationIfNotDuplicate(List<Reservation> existing, Reservation newReservation, ReservationDao)
```

Si ya existe una reserva con el mismo `user.getId()` y `service.getId()`, debe
lanzarse `DuplicateReservationException`. Caso contrario, debe persistirse con el DAO
y devolverse la reserva.

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

