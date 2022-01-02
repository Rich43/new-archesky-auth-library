package com.archesky.auth.library.model;

import java.util.List;

public class Role {
    private final String roleName;
    private final List<String> roles;

    public Role(final String roleName, final List<String> roles) {
        this.roleName = roleName;
        this.roles = roles;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<String> getRoles() {
        return roles;
    }
}
