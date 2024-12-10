package com.lmdeveloper.connect_sheet.service;

import com.lmdeveloper.connect_sheet.model.User;

import java.util.List;

public interface UserService {
    User save(User user);
    List<User> findAll();
    User findById(String id);
    User update(String id, User user);
    void deleteById(String id);
}
