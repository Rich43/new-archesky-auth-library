package com.archesky.auth.library.service;

import com.archesky.auth.library.model.Token;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenMappingService {
    private final Map<String, Token> userTokenMap = new HashMap<>();

    public Map<String, Token> getUserTokenMap() {
        return userTokenMap;
    }
}
