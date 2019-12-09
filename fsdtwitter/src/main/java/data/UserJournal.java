package data;

import data.models.User;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserJournal {

    private SegmentedJournal<User> sj;

    public UserJournal(String logName) {
        Serializer s = new SerializerBuilder()
                .addType(User.class)
                .build();

        sj = SegmentedJournal.<User>builder()
                .withName(logName)
                .withSerializer(s)
                .build();
    }

    public void writeJournal(User u) {
        SegmentedJournalWriter<User> w = sj.writer();
        w.append(u);
        CompletableFuture.supplyAsync(() -> {
            w.flush();
            return null;
        }).thenRun(() -> {
            w.close();
        });
    }

    public void writeJournal(Map<String, User> mu) {
        SegmentedJournalWriter<User> w = sj.writer();

        for(User u : mu.values()) {
            w.append(u);
        }

        CompletableFuture.supplyAsync(() -> {
            w.flush();
            return null;
        }).thenRun(() -> {
            w.close();
        });
    }

    public Map<String, User> readJournal(int entry) {
        Map<String, User> res = new HashMap<>();
        SegmentedJournalReader<User> r = sj.openReader(entry);
        User u;

        while(r.hasNext()) {
            u = r.next().entry();
            res.put(u.getName(), u);
        }

        CompletableFuture.supplyAsync(() -> {
            r.close();
            return null;
        });

        return res;
    }

    public Map<String, User> readAllJournal() {
        return readJournal(0);
    }

}
