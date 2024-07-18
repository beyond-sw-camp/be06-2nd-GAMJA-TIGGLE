package com.gamja.tiggle.user.application.service;

import com.gamja.tiggle.payment.application.port.out.PaymentPersistencePort;
import com.gamja.tiggle.user.application.port.in.SignupUserCommand;
import com.gamja.tiggle.user.application.port.in.SignupUserUseCase;
import com.gamja.tiggle.user.application.port.out.UserPersistencePort;
import com.gamja.tiggle.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignupUserService implements SignupUserUseCase {
    private final UserPersistencePort userPersistencePort;

    @Override
    public void signup(SignupUserCommand command) {
        User user = User.builder()

                .name(command.getName())
                .email(command.getEmail())
                .password(command.getPassword())
                .loginType(command.getLoginType())
                .status(command.getStatus())
                .enable(command.getEnable())
                .region_1depth_name(command.getRegion_1depth_name())
                .region_2depth_name(command.getRegion_2depth_name())
                .region_3depth_name(command.getRegion_3depth_name())
                .region_4depth_name(command.getRegion_4depth_name())
                .phoneNumber(command.getPhoneNumber())
                .build();

        userPersistencePort.saveUser(user);
    }
}
