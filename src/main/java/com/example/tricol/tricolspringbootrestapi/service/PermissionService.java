package com.example.tricol.tricolspringbootrestapi.service;

import com.example.tricol.tricolspringbootrestapi.model.UserApp;

import java.util.Set;

public interface PermissionService {
    Set<String> calculateFinalPermissions(UserApp user);
}
