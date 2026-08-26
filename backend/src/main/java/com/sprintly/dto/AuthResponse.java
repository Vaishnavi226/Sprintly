package com.sprintly.dto;

public class AuthResponse {
    private String token;
    private Long id;
    private String username;
    private String role;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String username, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public static AuthResponseBuilder builder() { return new AuthResponseBuilder(); }

    public static class AuthResponseBuilder {
        private String token;
        private Long id;
        private String username;
        private String role;

        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder id(Long id) { this.id = id; return this; }
        public AuthResponseBuilder username(String username) { this.username = username; return this; }
        public AuthResponseBuilder role(String role) { this.role = role; return this; }
        public AuthResponse build() { return new AuthResponse(token, id, username, role); }
    }
}
