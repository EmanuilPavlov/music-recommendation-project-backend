package com.example.student_system.musicproject.dto.user_request.interfaces;

public interface LimitedRequest {

    int DEFAULT_LIMIT = 10;

    Integer limit();

    default int resolvedLimit() {
        return limit() != null
                ? limit()
                : DEFAULT_LIMIT;
    }
}