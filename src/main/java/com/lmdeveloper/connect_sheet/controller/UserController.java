package com.lmdeveloper.connect_sheet.controller;

import com.lmdeveloper.connect_sheet.model.User;
import com.lmdeveloper.connect_sheet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping
    public User save(@RequestBody User user) {
        return this.service.save(user);
    }

    @GetMapping
    public List<User> findAll() {
        return this.service.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable String id) {
        return this.service.findById(id);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable String id, @RequestBody User user) {
        return this.service.update(id, user);
    }

    @DeleteMapping("/{id}")
    public String deleteById(@PathVariable String id) {
        this.service.deleteById(id);
        return "User deleted";
    }

}
