package com.example.student_system.musicproject.services.classes;

import com.example.student_system.musicproject.dto.UserSearchHistoryDTO;
import com.example.student_system.musicproject.entities.UserSearchHistory;
import com.example.student_system.musicproject.mappers.UserMapper;
import com.example.student_system.musicproject.repository.UserRepository;
import com.example.student_system.musicproject.repository.UserSearchHistoryRepository;
import com.example.student_system.musicproject.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserSearchHistoryRepository userSearchHistoryRepository;
    private final UserMapper userMapper;

    @Override
    public void saveSearch(
            String firebaseUid,
            String typeOfSearch,
            Integer limitRequested) {

        try {
            UserSearchHistory search = new UserSearchHistory();

            search.setUser(userRepository.findByFirebaseUid(firebaseUid));
            search.setTypeOfSearch(typeOfSearch);
            search.setLimitRequested(limitRequested);
            search.setSearchTimestamp(LocalDateTime.now());

            userSearchHistoryRepository.save(search);

            log.info(
                    "Saved search for user: {}, mood: {}, limit: {}",
                    firebaseUid,
                    typeOfSearch,
                    limitRequested);

        } catch (Exception e) {
            log.error("Failed to save search", e);
            throw e;
        }
    }

    @Override
    public List<UserSearchHistoryDTO> getSearchHistory(String firebaseUid) {
        try {
            log.info("Retrieved search history for user: {}", firebaseUid);

            return userSearchHistoryRepository
                    .findUserSearchHistoriesByUser(
                            userRepository.findByFirebaseUid(firebaseUid)
                    )
                    .stream()
                    .map(userMapper::toSearchHistoryDTO)
                    .toList();

        } catch (Exception e) {
            log.error("Failed to retrieve search history", e);
            throw e;
        }
    }
}