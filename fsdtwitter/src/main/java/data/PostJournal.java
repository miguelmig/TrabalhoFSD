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

    public PostJournal() {
        Serializer s = new SerializerBuilder()
                .addType(Post.class)
                .build();

        this.sj = SegmentedJournal.<Post>builder()
                .withName(JournalConfig.getPostsLogName())
                .withSerializer(s)
                .build();
    }

    public void writeJournal(Post p) {
        SegmentedJournalWriter<Post> w = sj.writer();
        w.append(p);
        CompletableFuture.supplyAsync(() -> {
            w.flush();
            return null;
        }).thenRun(() -> {
            w.close();
        });
    }

    public void writeJournal(Map<Integer, Post> mp) {
        SegmentedJournalWriter<Post> w = sj.writer();

        for(Post p : mp.values()) {
            w.append(p);
        }

        CompletableFuture.supplyAsync(() -> {
            w.flush();
            return null;
        }).thenRun(() -> {
            w.close();
        });
    }

    public Map<Integer, Post> readJournal(int entry) {
        Map<Integer, Post> res = new HashMap<>();
        SegmentedJournalReader<Post> r = sj.openReader(entry);
        Post p;

        while(r.hasNext()) {
            p = r.next().entry();
            res.put(p.getId(), p);
        }

        CompletableFuture.supplyAsync(() -> {
            r.close();
            return null;
        });

        return res;
    }

    public Map<Integer, Post> readAllJournal() {
        return readJournal(0);
    }
}
