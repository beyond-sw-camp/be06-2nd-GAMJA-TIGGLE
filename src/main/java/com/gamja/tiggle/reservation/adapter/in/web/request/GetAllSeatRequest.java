package com.gamja.tiggle.reservation.adapter.in.web.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetAllSeatRequest {

    private Long programId;
    private Long timesId;
    private Long sectionId;
}
