package com.lmdeveloper.connect_sheet.service.impl;

import com.lmdeveloper.connect_sheet.model.User;
import com.lmdeveloper.connect_sheet.repository.UserRepository;
import com.lmdeveloper.connect_sheet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    public User save(User user) {
        user.setId(UUID.randomUUID().toString());
        return this.repository.save(user, "Users!A2:C");
    }

    @Override
    public List<User> findAll() {
        return this.repository.findAll("Users!A2:C");
    }

    @Override
    public User findById(String id) {
        return this.repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User update(String id, User user) {
        User userFinded = this.findById(id);
        userFinded.update(user);

        return this.repository.save(userFinded, "Users!A2:C");
    }

    @Override
    public void deleteById(String id) {
        this.repository.deleteById(id);
    }
}
