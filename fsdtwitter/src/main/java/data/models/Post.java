package data.models;

import java.time.LocalDate;

public class Post {

    private int id;
    private LocalDate date;
    private String content;
    private String poster;

    public Post(int id, String content, String poster) {
        this.id = id;
        this.date = LocalDate.now();
        this.content = content;
        this.poster = poster;
    }

    public int getId() {
        return this.id;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getContent() {
        return this.content;
    }

    public String getPoster() {
        return this.poster;
    }
}
