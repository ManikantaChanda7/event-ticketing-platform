package com.eventhub.backend.service;

import com.eventhub.backend.dto.RegisterRequest;
import com.eventhub.backend.entity.User;

public interface UserService {

    User registerUser(RegisterRequest request);

    String loginUser(String email, String password);
}