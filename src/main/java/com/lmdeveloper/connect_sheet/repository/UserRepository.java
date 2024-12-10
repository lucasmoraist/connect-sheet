package com.lmdeveloper.connect_sheet.repository;

import com.lmdeveloper.connect_sheet.model.User;

import java.util.List;

public interface UserRepository {
    User save(User user, String RANGE);
    List<User> findAll(String RANGE);
}
