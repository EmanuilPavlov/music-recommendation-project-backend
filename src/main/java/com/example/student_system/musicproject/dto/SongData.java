package com.example.student_system.musicproject.dto;

public record SongData(
        String title,
        String artist,
        String artwork,
        int bpm,
        String audioUrl,
        double duration
) {}
