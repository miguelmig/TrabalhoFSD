package data.models;

import java.util.List;

public class User {

    private String name;
    private String pw;
    private List<String> subTags;

    public User(String name, String pw, List<String> subTags) {
        this.name = name;
        this.pw = pw;
        this.subTags = subTags;
    }

    public String getName() {
        return this.name;
    }

    public String getPW() {
        return this.pw;
    }

    public List<String> getTags {
        return this.subTags
    }

}