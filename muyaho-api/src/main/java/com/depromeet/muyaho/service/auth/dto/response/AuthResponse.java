package com.depromeet.muyaho.service.auth.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthResponse {

    private final String sessionId;

    public static AuthResponse of(String sessionId) {
        return new AuthResponse(sessionId);
    }

}
