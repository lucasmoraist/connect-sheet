package com.lmdeveloper.connect_sheet.model;

public class User {

    private String id;
    private String name;
    private String email;

    public User() {}

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void update(User user) {
        if (user.getName() != null) {
            this.name = user.getName();
        }
        if (user.getEmail() != null) {
            this.email = user.getEmail();
        }
    }

    public static class Builder {
        private String id;
        private String name;
        private String email;

        public Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public User build() {
            return new User(this.id, this.name, this.email);
        }
    }
}
