package data.models;

import java.time.LocalDate;

public class Post {

    private int id;
    private LocalDate date;
    private String content;
    private User poster;

    public Post(int id, String content, User poster) {

    }

}
