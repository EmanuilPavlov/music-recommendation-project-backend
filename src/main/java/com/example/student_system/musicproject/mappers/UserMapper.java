package com.example.student_system.musicproject.mappers;

import com.example.student_system.musicproject.dto.UserSearchHistoryDTO;
import com.example.student_system.musicproject.dto.auth.records.RegisterDTO;
import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.entities.UserSearchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "firebaseUid", source = "firebaseUid")
    User toEntity(String firebaseUid, RegisterDTO registerDTO);

    @Mapping(target = "firebaseUid", source = "user.firebaseUid")
    UserSearchHistoryDTO toSearchHistoryDTO(UserSearchHistory userSearchHistory);
}
