package com.example.student_system.musicproject.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_search_history")
public class UserSearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firebase_uid", referencedColumnName = "firebase_uid", nullable = false)
    private User user;

    @Column(name = "type_of_search", nullable = false)
    private String typeOfSearch;

    @Column(name = "limit_requested", nullable = false)
    private Integer limitRequested;

    @OneToMany(mappedBy = "searchHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SongRecommendation> results = new ArrayList<>();

    @Column(name = "search_timestamp", nullable = false, updatable = false)
    private LocalDateTime searchTimestamp;

    @PrePersist
    protected void onCreate() {
        searchTimestamp = LocalDateTime.now();
    }
}