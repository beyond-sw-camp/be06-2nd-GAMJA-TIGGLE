package com.gamja.tiggle.reservation.application.port.out;

import com.gamja.tiggle.common.BaseException;
import com.gamja.tiggle.reservation.domain.Seat;

import java.util.List;

public interface GetSeatPort {
    List<Seat> getAvailableSeatByQuery(Long programId, Long timesId, Long sectionId);

    List<Seat> getAvailableSeatByJpa(Long programId, Long timesId, Long sectionId) throws BaseException;

    List<Seat> getAllSeat(Long programId, Long sectionId, Long timesId) throws BaseException;
}
