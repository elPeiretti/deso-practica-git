package dto.user;

import domain.TouristService;

import java.time.Instant;

public record UserSummaryDTO(
    String username,
    int reservationCount,
    double totalSpent,
    TouristService mostExpensiveService,
    Instant mostRecentReservationDate) {}
