package com.example.student_system.musicproject.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "song_recommendations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "artwork")
    private String artwork;

    @Column(name = "artist")
    private String artist;

    @Column(name = "bpm", nullable = false)
    private int bpm;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "duration", nullable = false)
    private double duration;

    @Column(name = "mood")
    private String mood;

    @Column(name = "medical_condition")
    private String medicalCondition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_history_id", nullable = false)
    private UserSearchHistory searchHistory;
}