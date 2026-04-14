package com.example.lucidplus_onboarding.service;

import com.example.lucidplus_onboarding.dto.request.LoginRequest;
import com.example.lucidplus_onboarding.dto.request.OtpVerifyRequest;
import com.example.lucidplus_onboarding.dto.request.RegisterRequest;
import com.example.lucidplus_onboarding.dto.response.LoginResponse;
import com.example.lucidplus_onboarding.entity.Account;
import com.example.lucidplus_onboarding.entity.User;
import com.example.lucidplus_onboarding.enums.UserStatus;
import com.example.lucidplus_onboarding.exception.InvalidOtpException;
import com.example.lucidplus_onboarding.exception.UserNotFoundException;
import com.example.lucidplus_onboarding.repository.AccountRepository;
import com.example.lucidplus_onboarding.repository.UserRepository;
import com.example.lucidplus_onboarding.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already registered");
        if (userRepository.existsByMobile(request.getMobile()))
            throw new RuntimeException("Mobile already registered");

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .otp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5))
                .status(UserStatus.PENDING)
                .build();

        userRepository.save(user);


        System.out.println("OTP for " + request.getMobile() + " : " + otp);
        return otp;
    }

    @Transactional
    public void verifyOtp(OtpVerifyRequest request) {
        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getOtp().equals(request.getOtp()))
            throw new InvalidOtpException("Invalid OTP");

        if (user.getOtpExpiry().isBefore(LocalDateTime.now()))
            throw new InvalidOtpException("OTP has expired");

        user.setStatus(UserStatus.ACTIVE);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);


        Account account = Account.builder()
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .build();
        accountRepository.save(account);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByMobile(request.getIdentifier())
                .or(() -> userRepository.findByEmail(request.getIdentifier()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE)
            throw new RuntimeException("User not activated. Please verify OTP first.");

        String token = jwtUtil.generateToken(user.getMobile());
        return new LoginResponse(token, user.getName(), user.getEmail(), user.getMobile());
    }
}