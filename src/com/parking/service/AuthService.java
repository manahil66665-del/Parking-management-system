package com.parking.service;

import com.parking.dao.UserDao;
import com.parking.model.User;
import com.parking.util.PasswordUtil;
import com.parking.util.ValidationUtil;

import java.util.Optional;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User login(String username, String password) {
        ValidationUtil.requireText(username, "Username");
        ValidationUtil.requireText(password, "Password");
        Optional<User> user = userDao.findByUsername(username.trim());
        if (user.isEmpty() || !user.get().isActive()
                || !PasswordUtil.verifyPassword(password, user.get().getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user.get();
    }
}
