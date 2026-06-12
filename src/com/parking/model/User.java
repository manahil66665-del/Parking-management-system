package com.parking.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role rol;
    private boolean active;

    public User(int id, String username, String passwordHash, String fullName, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.rol = rol;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return rol;
    }

    public boolean isActive() {
        return active;
    }
}
