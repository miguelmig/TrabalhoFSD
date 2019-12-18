package data;

import config.JournalConfig;
import data.models.Post;
import data.models.User;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PostJournal {

    private SegmentedJournal<Post> sj;

    public PostJournal(String logName) {
        Serializer s = new SerializerBuilder()
                .addType(Post.class)
                .build();

        this.sj = SegmentedJournal.<Post>builder()
                .withName(logName)
                .withSerializer(s)
                .build();
    }

    public void writeJournal(Post p) {
        SegmentedJournalWriter<Post> writer = sj.writer();
        writer.append(p);
        CompletableFuture.supplyAsync(() -> {
            writer.flush();
            return null;
        }).thenRun(writer::close);
    }

    public void writeJournal(Map<Integer, Post> mp) {
        SegmentedJournalWriter<Post> writer = sj.writer();

        for(Post p : mp.values()) {
            writer.append(p);
        }

        CompletableFuture.supplyAsync(() -> {
            writer.flush();
            return null;
        }).thenRun(writer::close);
    }

    public Map<Integer, Post> readJournal(int entry) {
        Map<Integer, Post> res = new HashMap<>();
        SegmentedJournalReader<Post> reader = sj.openReader(entry);
        Post p;

        while(reader.hasNext()) {
            p = reader.next().entry();
            res.put(p.getId(), p);
        }

        CompletableFuture.supplyAsync(() -> {
            reader.close();
            return null;
        });

        return res;
    }

    public Map<Integer, Post> readAllJournal() {
        return readJournal(0);
    }
}
