package com.piisw.cinema_tickets_app.api;

import com.piisw.cinema_tickets_app.domain.auditedobject.entity.ObjectState;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreeningDTO {

    @ApiModelProperty(readOnly = true)
    private Long screeningId;

    @NotNull
    private Long movieId;

    @NotNull
    private Long screeningRoomId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private String price;

    @ApiModelProperty(readOnly = true)
    ObjectState objectState;

    @ApiModelProperty(readOnly = true)
    private ScreeningRoomDTO screeningRoom;
}
