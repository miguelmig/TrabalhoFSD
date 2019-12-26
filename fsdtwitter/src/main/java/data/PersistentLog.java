package data;

import data.models.Post;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;
import net.StateMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PersistentLog {

    private SegmentedJournal<StateMessage> sj;

    public PersistentLog(String logName) {
        Serializer s = new SerializerBuilder()
                .addType(StateMessage.class)
                .build();

        this.sj = SegmentedJournal.<StateMessage>builder()
                .withName(logName)
                .withSerializer(s)
                .build();
    }

    public void write(StateMessage sm) {

        SegmentedJournalWriter<StateMessage> writer = sj.writer();
        writer.truncate(0);
        writer.append(sm);
        CompletableFuture.supplyAsync(() -> {
            writer.flush();
            return null;
        }).thenRun(writer::close);
    }

    public StateMessage read() {
        SegmentedJournalReader<StateMessage> reader = sj.openReader(0);
        StateMessage sm;

        sm = reader.getCurrentEntry().entry();

        CompletableFuture.supplyAsync(() -> {
            reader.close();
            return null;
        });

        return sm;
    }
}
