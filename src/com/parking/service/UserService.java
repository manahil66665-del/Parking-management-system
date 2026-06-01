package com.parking.service;

import com.parking.dao.UserDao;
import com.parking.model.User;
import com.parking.util.PasswordUtil;
import com.parking.util.ValidationUtil;

import java.util.List;

public class UserService {
    private final UserDao userDao = new UserDao();

    public List<User> allUsers() {
        return userDao.findAll();
    }

    public void createUser(String username, String password, String fullName, String roleName) {
        ValidationUtil.requireText(username, "Username");
        ValidationUtil.requireText(password, "Password");
        ValidationUtil.requireText(fullName, "Full name");
        ValidationUtil.requireText(roleName, "Role");
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!"ADMIN".equalsIgnoreCase(roleName) && !"STAFF".equalsIgnoreCase(roleName)) {
            throw new IllegalArgumentException("Role must be ADMIN or STAFF.");
        }
        userDao.create(username.trim(), PasswordUtil.hashPassword(password), fullName.trim(), roleName.toUpperCase());
    }

    public void setActive(int targetUserId, boolean active, User currentUser) {
        if (currentUser != null && currentUser.getId() == targetUserId && !active) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }
        userDao.setActive(targetUserId, active);
    }

    public void updateUser(int targetUserId, String username, String password, String fullName, String roleName,
            User currentUser) {
        ValidationUtil.requireText(username, "Username");
        ValidationUtil.requireText(fullName, "Full name");
        ValidationUtil.requireText(roleName, "Role");
        if (!"ADMIN".equalsIgnoreCase(roleName) && !"STAFF".equalsIgnoreCase(roleName)) {
            throw new IllegalArgumentException("Role must be ADMIN or STAFF.");
        }
        if (password != null && !password.isBlank() && password.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }
        if (currentUser != null && currentUser.getId() == targetUserId
                && !"ADMIN".equalsIgnoreCase(roleName)) {
            throw new IllegalArgumentException("You cannot remove admin role from your own account.");
        }
        String passwordHash = password == null || password.isBlank() ? null : PasswordUtil.hashPassword(password);
        userDao.update(targetUserId, username.trim(), fullName.trim(), roleName.toUpperCase(), passwordHash);
    }

    public void deleteUser(int targetUserId, User currentUser) {
        if (currentUser != null && currentUser.getId() == targetUserId) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        userDao.delete(targetUserId);
    }
}
