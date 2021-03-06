package com.piisw.cinema_tickets_app.domain.seat.boundary;

import com.piisw.cinema_tickets_app.api.SeatDTO;
import com.piisw.cinema_tickets_app.domain.auditedobject.entity.ObjectState;
import com.piisw.cinema_tickets_app.domain.screening.control.ScreeningService;
import com.piisw.cinema_tickets_app.domain.screening.entity.Screening;
import com.piisw.cinema_tickets_app.domain.seat.control.SeatService;
import com.piisw.cinema_tickets_app.domain.seat.entity.SeatAvailabilityDetails;
import com.piisw.cinema_tickets_app.infrastructure.security.UserInfo;
import com.piisw.cinema_tickets_app.infrastructure.security.validation.HasAnyRole;
import com.piisw.cinema_tickets_app.infrastructure.security.validation.LoggedUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.piisw.cinema_tickets_app.infrastructure.utils.ResourcePath.ID;
import static com.piisw.cinema_tickets_app.infrastructure.utils.ResourcePath.ID_PATH;
import static com.piisw.cinema_tickets_app.infrastructure.utils.ResourcePath.OBJECT_STATE;

@Api(tags = "Seats")
@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final ScreeningService screeningService;
    private final SeatService seatService;
    private final SeatMapper seatMapper;

    @ApiOperation(value = "${api.seats.get.value}", notes = "${api.seats.get.notes}")
    @GetMapping("/screening" + ID_PATH)
    @HasAnyRole
    public List<SeatDTO> getSeatsForScreening(@PathVariable(ID) Long screeningId,
                                              @RequestParam(value = OBJECT_STATE, defaultValue = "ACTIVE") Set<ObjectState> objectStates,
                                              @ApiIgnore  @LoggedUser UserInfo currentUser) {
        Screening screening = screeningService.getScreeningById(screeningId, objectStates);
        List<SeatAvailabilityDetails> seats = seatService.getSeatsDetailsForScreening(screening, objectStates, currentUser.getId());
        return seats.stream()
                .map(seatMapper::mapToSeatDTO)
                .collect(Collectors.toList());
    }

}
