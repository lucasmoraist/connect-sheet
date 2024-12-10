package com.lmdeveloper.connect_sheet.view;

import com.lmdeveloper.connect_sheet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UserService service;

    @GetMapping("/")
    public String home() {
        return "index";
    }

}
