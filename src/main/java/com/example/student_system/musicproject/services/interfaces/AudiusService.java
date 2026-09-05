package com.example.student_system.musicproject.services.interfaces;

import com.example.student_system.musicproject.dto.SongData;

import java.util.List;

public interface AudiusService {
    List<SongData> searchTracks(String genre, Integer bpmMin, Integer bpmMax, int limit);
}