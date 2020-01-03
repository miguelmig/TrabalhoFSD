package server;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.List;
import java.util.concurrent.Executor;

public class Coordinator {

    public static final byte[] DUMMY = new byte[1];
    private static final String CLOGNAME = "cLog";
    private static final String VLOGNAME = "vLog";
    private static final String SLOGNAME = "sLog";



    class PayloadWrapper {
        public byte[] pl;

        PayloadWrapper(byte[] payload) {
            this.pl = payload;
        }
    }


    ManagedMessagingService ms;
    Serializer s;
    Executor e;
    SegmentedJournal<Character> sjDecision;
    SegmentedJournalWriter<Character> wD;
    SegmentedJournal<PayloadWrapper> sjPayload;
    SegmentedJournalWriter<PayloadWrapper> wP;
    List<Address> lAdd;
    DecisionArray vereditos;
    Transflag inTransaction;


    public Coordinator(String cName, int port, Executor e, List<Address> lAdd) {

        this.ms = new NettyMessagingService(cName,
                Address.from(port),
                new MessagingConfig());

        this.s = new SerializerBuilder()
                .addType(PayloadWrapper.class)
                .build();
        this.e = e;

        this.sjDecision = SegmentedJournal.<Character>builder()
                .withName(CLOGNAME)
                .withSerializer(this.s)
                .build();


        this.wD = this.sjDecision.writer();

        this.sjPayload = SegmentedJournal.<PayloadWrapper>builder()
                .withName(VLOGNAME)
                .withSerializer(this.s)
                .build();

        this.wP = this.sjPayload.writer();

        this.lAdd = lAdd;
        this.vereditos = new DecisionArray(lAdd.size());
        this.inTransaction = new Transflag();
    }

    public void registerHandlers() {
        this.ms.registerHandler("request", (origem, payload) -> {


            //tentativa de aquisicao da flag de começo de transacao
            try {
                this.inTransaction.canStartTransaction();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("Transaction started!");

            PayloadWrapper pw = new PayloadWrapper(payload);
            SegmentedJournalWriter<PayloadWrapper> wP

            this.wP.append(pw);

            Utils.closeWriter(wP);

            broadcastMsg("areuready", Coordinator.DUMMY);

            try {
                Thread.sleep(5000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Decision d = this.vereditos.haveDecision();
            SegmentedJournalWriter<Character> w = sjDecision.writer();

            if (d == Decision.COMMIT) {
                sendDecision(w, 'C', "new", payload);
                System.out.println("[Coordinator]Confirm!");
            }
            else if(d == Decision.ABORT){
                sendDecision(w, 'A', "abort", DUMMY);
                System.out.println("[Coordinator]Abort!");
            }
            else {
                sendDecision(w, 'A', "abort", DUMMY);
                System.out.println("[Coordinator]Someone did not answer! Is he dead?");
            }

            // fim da transacao e aviso

            this.vereditos.zeroOut();
            this.dumpJournals();
            this.inTransaction.endTransaction();
        }, e);

        this.ms.registerHandler("ready", (origem, payload) -> {
            int id = origem.port() - 12345;
            System.out.println("[Coordinator]" + origem.host() + " :: " +  origem.port() +  " -- esta pronto.");
            this.vereditos.addDecision(Decision.COMMIT, id);

        }, e);

        this.ms.registerHandler("abort", (origem, payload) -> {
            int id = origem.port() - 12345;
            System.out.println("[Coordinator]" + origem.host() + " :: " +  origem.port() +  " -- pediu um abort.");
            this.vereditos.addDecision(Decision.ABORT, id);
        }, e);
    }

    private void broadcastMsg(String msgType, byte[] pl) {
        for(Address ads : this.lAdd) {
            System.out.println("[Coordinator] Sending too " + ads.host() + " " + ads.port());
            this.ms.sendAsync(ads, msgType, pl);
        }
    }

    private void sendDecision(SegmentedJournalWriter<Character> w, Character je, String msgType, byte[] pl) {
        w.append(je);
        Utils.closeWriter(w);
        this.broadcastMsg(msgType, pl);
    }

    private void dumpJournals() {
        Utils.closeWriter(this.wD);
        Utils.closeWriter(this.wP);

        /*
        CompletableFuture.supplyAsync(() -> {
            wd.close();
            wp.close();
            return null;
        });

         */
    }

    public void startMS() {
        this.ms.start();
    }

    public void stopMS() {
        this.ms.stop();
    }
}
