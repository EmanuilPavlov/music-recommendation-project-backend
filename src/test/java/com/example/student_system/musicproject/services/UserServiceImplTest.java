package com.example.student_system.musicproject.services;

import com.example.student_system.musicproject.dto.UserSearchHistoryDTO;
import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.entities.UserSearchHistory;
import com.example.student_system.musicproject.mappers.UserMapper;
import com.example.student_system.musicproject.repository.UserRepository;
import com.example.student_system.musicproject.repository.UserSearchHistoryRepository;
import com.example.student_system.musicproject.services.classes.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSearchHistoryRepository userSearchHistoryRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSearchHistoryDTO searchHistoryDTO;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<UserSearchHistory> searchCaptor;

    private User user;
    private UserSearchHistory searchHistory;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirebaseUid("firebase-uid");
        user.setEmail("test@example.com");

        searchHistory = new UserSearchHistory();
        searchHistory.setUser(user);
        searchHistory.setTypeOfSearch("Happy");
        searchHistory.setLimitRequested(10);
    }

    @Nested
    @DisplayName("Save Search Tests")
    class SaveSearchTests {

        @Test
        @DisplayName("saveSearch - Success")
        void testSaveSearch_Success() {
            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(user);

            when(userSearchHistoryRepository.save(any(UserSearchHistory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            userService.saveSearch(
                    "firebase-uid",
                    "Happy",
                    10
            );

            verify(userRepository)
                    .findByFirebaseUid("firebase-uid");

            verify(userSearchHistoryRepository)
                    .save(searchCaptor.capture());

            UserSearchHistory savedSearch = searchCaptor.getValue();

            assertNotNull(savedSearch);
            assertEquals(user, savedSearch.getUser());
            assertEquals("Happy", savedSearch.getTypeOfSearch());
            assertEquals(10, savedSearch.getLimitRequested());
            assertNotNull(savedSearch.getSearchTimestamp());
        }

        @Test
        @DisplayName("saveSearch - Repository Error")
        void testSaveSearch_RepositoryError() {
            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(user);

            when(userSearchHistoryRepository.save(any(UserSearchHistory.class)))
                    .thenThrow(new RuntimeException("Database error"));

            RuntimeException exception =
                    assertThrows(
                            RuntimeException.class,
                            () -> userService.saveSearch(
                                    "firebase-uid",
                                    "Happy",
                                    10
                            )
                    );

            assertEquals(
                    "Database error",
                    exception.getMessage()
            );

            verify(userRepository)
                    .findByFirebaseUid("firebase-uid");

            verify(userSearchHistoryRepository)
                    .save(any(UserSearchHistory.class));
        }
    }

    @Nested
    @DisplayName("Search History Tests")
    class SearchHistoryTests {

        @Test
        @DisplayName("getSearchHistory - Success")
        void testGetSearchHistory_Success() {
            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(user);

            when(userSearchHistoryRepository.findUserSearchHistoriesByUser(user))
                    .thenReturn(List.of(searchHistory));

            when(userMapper.toSearchHistoryDTO(searchHistory))
                    .thenReturn(searchHistoryDTO);

            List<UserSearchHistoryDTO> results =
                    userService.getSearchHistory("firebase-uid");

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(searchHistoryDTO, results.get(0));

            verify(userRepository)
                    .findByFirebaseUid("firebase-uid");

            verify(userSearchHistoryRepository)
                    .findUserSearchHistoriesByUser(user);

            verify(userMapper)
                    .toSearchHistoryDTO(searchHistory);
        }

        @Test
        @DisplayName("getSearchHistory - Multiple Results")
        void testGetSearchHistory_MultipleResults() {
            UserSearchHistory secondSearch = new UserSearchHistory();
            secondSearch.setUser(user);
            secondSearch.setTypeOfSearch("Anxiety");
            secondSearch.setLimitRequested(5);

            UserSearchHistoryDTO secondDTO =
                    mock(UserSearchHistoryDTO.class);

            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(user);

            when(userSearchHistoryRepository.findUserSearchHistoriesByUser(user))
                    .thenReturn(
                            List.of(
                                    searchHistory,
                                    secondSearch
                            )
                    );

            when(userMapper.toSearchHistoryDTO(searchHistory))
                    .thenReturn(searchHistoryDTO);

            when(userMapper.toSearchHistoryDTO(secondSearch))
                    .thenReturn(secondDTO);

            List<UserSearchHistoryDTO> results =
                    userService.getSearchHistory("firebase-uid");

            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals(searchHistoryDTO, results.get(0));
            assertEquals(secondDTO, results.get(1));

            verify(userMapper)
                    .toSearchHistoryDTO(searchHistory);

            verify(userMapper)
                    .toSearchHistoryDTO(secondSearch);
        }

        @Test
        @DisplayName("getSearchHistory - Empty History")
        void testGetSearchHistory_EmptyHistory() {
            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(user);

            when(userSearchHistoryRepository.findUserSearchHistoriesByUser(user))
                    .thenReturn(List.of());

            List<UserSearchHistoryDTO> results =
                    userService.getSearchHistory("firebase-uid");

            assertNotNull(results);
            assertTrue(results.isEmpty());

            verify(userRepository)
                    .findByFirebaseUid("firebase-uid");

            verify(userSearchHistoryRepository)
                    .findUserSearchHistoriesByUser(user);

            verify(userMapper, never())
                    .toSearchHistoryDTO(any(UserSearchHistory.class));
        }

        @Test
        @DisplayName("getSearchHistory - Repository Error")
        void testGetSearchHistory_RepositoryError() {
            when(userRepository.findByFirebaseUid("firebase-uid"))
                    .thenReturn(user);

            when(userSearchHistoryRepository.findUserSearchHistoriesByUser(user))
                    .thenThrow(new RuntimeException("Database error"));

            RuntimeException exception =
                    assertThrows(
                            RuntimeException.class,
                            () -> userService.getSearchHistory(
                                    "firebase-uid"
                            )
                    );

            assertEquals(
                    "Database error",
                    exception.getMessage()
            );

            verify(userRepository)
                    .findByFirebaseUid("firebase-uid");

            verify(userSearchHistoryRepository)
                    .findUserSearchHistoriesByUser(user);
        }
    }
}