package data;

import config.JournalConfig;
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
        SegmentedJournalWriter<User> writer = sj.writer();
        writer.append(u);
        CompletableFuture.supplyAsync(() -> {
            writer.flush();
            return null;
        }).thenRun(writer::close);
    }

    public void writeJournal(Map<String, User> mu) {
        SegmentedJournalWriter<User> writer = sj.writer();

        for(User u : mu.values()) {
            writer.append(u);
        }

        CompletableFuture.supplyAsync(() -> {
            writer.flush();
            return null;
        }).thenRun(writer::close);
    }

    public Map<String, User> readJournal(int entry) {
        Map<String, User> res = new HashMap<>();
        SegmentedJournalReader<User> reader = sj.openReader(entry);
        User u;

        while(reader.hasNext()) {
            u = reader.next().entry();
            res.put(u.getName(), u);
        }

        CompletableFuture.supplyAsync(() -> {
            reader.close();
            return null;
        });

        return res;
    }

    public Map<String, User> readAllJournal() {
        return readJournal(0);
    }

}
