package data.models;

import java.time.LocalDate;
import java.util.List;

public class Post {

    private int id;
    private LocalDate date;
    private String content;
    private List<String> tags;
    private String poster;

    public Post(int id, String content, List<String> tags, String poster) {
        this.id = id;
        this.date = LocalDate.now();
        this.content = content;
        this.tags = tags;
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

    public List<String> getTags() {
        return this.tags;
    }

    public String getPoster() {
        return this.poster;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", date=" + date +
                ", content='" + content + '\'' +
                ", tags=" + tags +
                ", poster='" + poster + '\'' +
                '}';
    }
}
