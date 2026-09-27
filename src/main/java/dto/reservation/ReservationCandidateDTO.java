package dto.reservation;

import domain.Flight;
import domain.User;

public record ReservationCandidateDTO(User user, Flight flight) {}
