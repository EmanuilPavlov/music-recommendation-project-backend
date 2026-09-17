package com.example.student_system.musicproject.services.interfaces;

import com.example.student_system.musicproject.dto.BPMRange;
import com.example.student_system.musicproject.dto.SongData;

import java.util.List;

public interface AudiusService {
    List<SongData> fetchSongs(List<String> genres, List<BPMRange> bpmRanges, Integer limit);
}