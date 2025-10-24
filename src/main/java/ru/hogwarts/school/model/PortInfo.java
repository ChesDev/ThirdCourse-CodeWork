package ru.hogwarts.school.model;

public class PortInfo {
    private String port;
    private String profile;

    public PortInfo(String port, String profile) {
        this.port = port;
        this.profile = profile;
    }

    public String getPort() {
        return port;
    }

    public String getProfile() {
        return profile;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}