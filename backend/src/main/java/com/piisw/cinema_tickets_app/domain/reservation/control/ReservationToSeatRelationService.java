package com.piisw.cinema_tickets_app.domain.reservation.control;

import com.piisw.cinema_tickets_app.domain.auditedobject.control.AuditedObjectService;
import com.piisw.cinema_tickets_app.domain.auditedobject.entity.ObjectState;
import com.piisw.cinema_tickets_app.domain.reservation.entity.Reservation;
import com.piisw.cinema_tickets_app.domain.reservation.entity.ReservationToSeatRelation;
import com.piisw.cinema_tickets_app.domain.seat.entity.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;


@Service
@Transactional
@RequiredArgsConstructor
public class ReservationToSeatRelationService {

    private final ReservationToSeatRelationRepository reservationToSeatRelationRepository;
    private final ReservationToRelationSpecification specification;
    private final AuditedObjectService auditedObjectService;

    List<Seat> getReservedSeats(List<Reservation> reservations, Set<ObjectState> objectStates) {
        List<ReservationToSeatRelation> relations = getReservationToSeatRelationsForReservations(reservations, objectStates);
        return relations.stream()
                .map(ReservationToSeatRelation::getSeat)
                .collect(Collectors.toList());
    }

    Map<Seat, Long> getReservedSeatsWithUserId(List<Reservation> reservations, Set<ObjectState> objectStates) {
        List<ReservationToSeatRelation> relations = getReservationToSeatRelationsForReservations(reservations, objectStates);
        return relations.stream()
                .collect(toMap(ReservationToSeatRelation::getSeat, relation -> relation.getReservation().getReservedByUser()));
    }

    List<ReservationToSeatRelation> getReservationToSeatRelationsForReservations(List<Reservation> reservations, Set<ObjectState> objectStates) {
        Set<Long> reservationsIds = auditedObjectService.toSetOfIds(reservations);
        return reservationToSeatRelationRepository.findAll(specification.whereReservationIdAndObjectStateIn(reservationsIds, objectStates));
    }

    public List<ReservationToSeatRelation> createReservationToSeatRelation(Reservation reservation, Collection<Seat> seats) {
        List<ReservationToSeatRelation> relationsToCreate = seats.stream()
                .map(seat -> buildReservationToSeatRelation(reservation, seat))
                .collect(Collectors.toList());
        return reservationToSeatRelationRepository.saveAll(relationsToCreate);
    }

    private ReservationToSeatRelation buildReservationToSeatRelation(Reservation reservation, Seat seat) {
        return ReservationToSeatRelation.builder()
                .reservation(reservation)
                .seat(seat)
                .objectState(ObjectState.ACTIVE)
                .build();
    }

    List<ReservationToSeatRelation> removeRelationsForReservations(List<Reservation> reservations) {
        List<ReservationToSeatRelation> relationsToRemove = getReservationToSeatRelationsForReservations(reservations, Set.of(ObjectState.ACTIVE));
        relationsToRemove.forEach(relation -> relation.setObjectState(ObjectState.REMOVED));
        return reservationToSeatRelationRepository.saveAll(relationsToRemove);
    }

}
