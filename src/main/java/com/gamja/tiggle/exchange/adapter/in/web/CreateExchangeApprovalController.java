package com.gamja.tiggle.exchange.adapter.in.web;


import com.gamja.tiggle.common.BaseException;
import com.gamja.tiggle.common.BaseResponse;
import com.gamja.tiggle.common.BaseResponseStatus;
import com.gamja.tiggle.common.annotation.WebAdapter;
import com.gamja.tiggle.exchange.adapter.in.web.request.CreateExchangeApprovalRequest;
import com.gamja.tiggle.exchange.adapter.out.persistence.ExchangeEntity;
import com.gamja.tiggle.exchange.application.port.in.CreateExchangeApprovalCommand;
import com.gamja.tiggle.exchange.application.port.in.CreateExchangeApprovalUseCase;
import com.gamja.tiggle.user.domain.CustomUserDetails;
import com.gamja.tiggle.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;


@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/exchange")
@Tag(name = "Response For Exchange Request")
public class CreateExchangeApprovalController {
    private final CreateExchangeApprovalUseCase useCase;

    @PostMapping("/approval")
    @Operation(summary = "교환 요청 응답", description = "교환 요청 동의나 거절로 응답하는 API 입니다.")
    public BaseResponse<BaseResponseStatus> createApproval(@AuthenticationPrincipal CustomUserDetails customUserDetails , @RequestBody CreateExchangeApprovalRequest request) {
        User user = customUserDetails.getUser();

        CreateExchangeApprovalCommand command = CreateExchangeApprovalCommand.builder()
                .user(user)
                .exchangeId(request.getExchangeId())
                .isAgree(request.getIsAgree())
                .build();

        try {
//            TODO: 1번 실행
            ExchangeEntity exchangeEntity = useCase.findExchangeEntity(command.getExchangeId());

//            TODO: 2번 실행
            if (!Objects.equals(exchangeEntity.getReservation2().getUser().getId(), user.getId())) {
                throw new BaseException(BaseResponseStatus.WRONG_EXCHANGE_OFFER);
            }

            if(exchangeEntity.getIsSuccess() == null){
                command = CreateExchangeApprovalCommand.builder()
                        .user(user)
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
