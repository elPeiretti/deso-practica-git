package dto.reservation;

import java.util.List;

public record ValidationResultDTO(
    List<ReservationCandidateDTO> validCandidates, List<String> errors) {}
