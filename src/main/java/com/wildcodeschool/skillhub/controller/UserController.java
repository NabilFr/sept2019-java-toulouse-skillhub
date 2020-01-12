package com.wildcodeschool.skillhub.controller;

import com.wildcodeschool.skillhub.entity.User;
import com.wildcodeschool.skillhub.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class UserController {

    private UserRepository repository = new UserRepository();

    @PostMapping("/connect")
    public String connect(Model model, HttpSession session, @RequestParam(name="username") String username, @RequestParam(name="password") String password ) {

        Long userId = repository.checkUser(username, password);
        if (userId != null) {
            User user = repository.getUserById(userId);
            session.setAttribute("user", user);
            return "redirect:/feed";
        } else {
            return "index";
        }
    };

    @GetMapping("/disconnect")
    public String disconnect(Model model, HttpSession session) {
        session.setAttribute("user", null);
        return "index";
    }
}
