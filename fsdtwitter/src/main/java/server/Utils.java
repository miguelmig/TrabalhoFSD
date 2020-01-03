package server;

import io.atomix.storage.journal.SegmentedJournalWriter;

import java.util.concurrent.CompletableFuture;

public class Utils {

    public static <E> void closeWriter(SegmentedJournalWriter<E> w) {
        CompletableFuture.supplyAsync(() -> {
            w.flush();
            return null;
        });//.thenRun(w::close);
    }


}
