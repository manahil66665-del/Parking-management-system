package com.parking.service;

import com.parking.model.User;

public class AccessControlService {
    public boolean canManageBilling(User user) {
        return isAdmin(user) || hasRole(user, "STAFF");
    }

    public boolean canManageSlots(User user) {
        return isAdmin(user) || hasRole(user, "STAFF");
    }

    public boolean isAdmin(User user) {
        return hasRole(user, "ADMIN");
    }

    private boolean hasRole(User user, String roleName) {
        return user != null && user.getRole() != null && roleName.equalsIgnoreCase(user.getRole().getName());
    }
}
