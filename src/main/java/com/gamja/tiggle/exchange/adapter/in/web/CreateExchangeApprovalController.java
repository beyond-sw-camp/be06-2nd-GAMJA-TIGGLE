package com.gamja.tiggle.exchange.adapter.in.web;


import com.gamja.tiggle.common.BaseException;
import com.gamja.tiggle.common.BaseResponse;
import com.gamja.tiggle.common.BaseResponseStatus;
import com.gamja.tiggle.common.annotation.WebAdapter;
import com.gamja.tiggle.exchange.adapter.in.web.request.CreateExchangeApprovalRequest;
import com.gamja.tiggle.exchange.adapter.out.persistence.ExchangeEntity;
import com.gamja.tiggle.exchange.application.port.in.CreateExchangeApprovalCommand;
import com.gamja.tiggle.exchange.application.port.in.CreateExchangeApprovalUseCase;
import com.gamja.tiggle.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/exchange")
public class CreateExchangeApprovalController {
    private final CreateExchangeApprovalUseCase useCase;

    @PostMapping("/approval")
    public BaseResponse<BaseResponseStatus> createApproval(@RequestBody CreateExchangeApprovalRequest request) {
        CreateExchangeApprovalCommand command = CreateExchangeApprovalCommand.builder()
                .exchangeId(request.getExchangeId())
                .isAgree(request.getIsAgree())
                .build();

        User user = User.builder().id(1L).build();

        try {
//            나에게 온 요청이 아닐 때,
            ExchangeEntity exchangeEntity = useCase.findExchangeEntity(command.getExchangeId());

            if (!Objects.equals(exchangeEntity.getReservation2().getUser().getId(), user.getId())) {
                throw new BaseException(BaseResponseStatus.WRONG_USER_EXCHANGE_OFFER);
            }

            if(exchangeEntity.getIsSuccess() == null){
                command = CreateExchangeApprovalCommand.builder()
                        .exchangeId(exchangeEntity.getId())
                        .reservationId1(exchangeEntity.getReservation1().getId())
                        .reservationId2(exchangeEntity.getReservation2().getId())
                        .isAgree(request.getIsAgree())
                        .build();

                if (command.getIsAgree()) {
                    useCase.create(exchangeEntity, command);
                } else {
                    useCase.reject(exchangeEntity);
                    return new BaseResponse<>(BaseResponseStatus.EXCHANGE_REJECT_SUCCESS);
                }
            } else{
                if (exchangeEntity.getIsSuccess()) {
                    throw new BaseException(BaseResponseStatus.ALREADY_SUCCESS_EXCHANGE_OFFER);
                }else  {
                    throw new BaseException(BaseResponseStatus.ALREADY_FAIL_EXCHANGE_OFFER);
                }
            }

        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }

        return new BaseResponse<>(BaseResponseStatus.EXCHANGE_APPROVAL_SUCCESS);
    }


}