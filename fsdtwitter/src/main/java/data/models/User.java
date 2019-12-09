package data.models;

import java.util.List;

public class User {

    private String name;
    private String password;
    private List<String> subTags;

    public User(String name, String password, List<String> subTags) {
        this.name = name;
        this.password = password;
        this.subTags = subTags;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public List<String> getTags() {
        return this.subTags;
    }

}