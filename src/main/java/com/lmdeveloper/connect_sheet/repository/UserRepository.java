package com.lmdeveloper.connect_sheet.repository;

import com.lmdeveloper.connect_sheet.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user, String RANGE);
    List<User> findAll(String RANGE);
    Optional<User> findById(String id);
    void deleteById(String id);
}
