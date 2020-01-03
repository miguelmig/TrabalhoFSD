package net;

import config.Config;
import config.JournalConfig;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Coordinator {

    public static final byte[] DUMMY = new byte[1];
    private static final String DLOGNAME = "cLog";
    private static final String PLOGNAME = "vLog";
    private static final String SLOGNAME = "sLog";

    public static int calculaID(int port) {
        return port - Config.ADDR_START;
    }

    class PayloadWrapper {
        public byte[] pl;
        public int tid;
    }

    ManagedMessagingService ms;
    Serializer s;
    Executor e;
    SegmentedJournalWriter<Character> wD;
    SegmentedJournalWriter<PayloadWrapper> wP;
    List<Address> lAdd;
    DecisionArray vereditos;
    Transflag inTransaction;
    int tID;

    public Coordinator() {

        this.ms = new NettyMessagingService("Coordinator",
                Address.from(Config.COORD_ADDR),
                new MessagingConfig());

        this.s = new SerializerBuilder()
                .addType(PayloadWrapper.class)
                .build();

        this.e = Executors.newFixedThreadPool(2);

        SegmentedJournal<Character> sjDecision = SegmentedJournal.<Character>builder()
                .withName(JournalConfig.getDlog())
                .withSerializer(this.s)
                .build();

        SegmentedJournalReader<Character> rD = sjDecision.openReader(0);

        /*
        if(rD.hasNext()) {
            Character c = rD.next().entry();
            if(c == 'C') {

            }
            else {

            }
        }
         */


        this.wD = sjDecision.writer();

        SegmentedJournal<PayloadWrapper> sjPayload = SegmentedJournal.<PayloadWrapper>builder()
                .withName(JournalConfig.getPlog())
                .withSerializer(this.s)
                .build();

        this.wP = sjPayload.writer();

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

            PayloadWrapper pw = new PayloadWrapper();
            pw.pl = payload;
            pw.tid = this.tID;

            this.wP.append(pw);
            this.wP.flush();


            broadcastMsg("areuready", this.s.encode(this.tID));

            try {
                Thread.sleep(3000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Decision d = this.vereditos.haveDecision();

            if (d == Decision.COMMIT) {
                sendDecision(this.wD, 'C', "new", payload);
                System.out.println("[Coordinator]Confirm!");
            }
            else if(d == Decision.ABORT){
                sendDecision(this.wD, 'A', "abort", DUMMY);
                System.out.println("[Coordinator]Abort!");
            }
            else {
                sendDecision(this.wD, 'A', "abort", DUMMY);
                System.out.println("[Coordinator]Someone did not answer! Is he dead?");
            }


            // fim da transacao - apagar informação relativa a esta ultima.
            this.vereditos.zeroOut();
            this.tID++;
            this.wD.truncate(0);
            this.inTransaction.endTransaction();
        }, e);

        this.ms.registerHandler("ready", (origem, payload) -> {
            int sid = calculaID(origem.port());
            int tID = s.decode(payload);

            if(tID == this.tID) {
                System.out.println("[Coordinator]" + origem.host() + " :: " + origem.port() + " -- esta pronto.");
                this.vereditos.addDecision(Decision.COMMIT, sid);
            }
            else
                System.out.println("[Coordinator]Late response!");

        }, e);

        this.ms.registerHandler("abort", (origem, payload) -> {
            int id = calculaID(origem.port());
            int tID = s.decode(payload);

            if(tID == this.tID) {
                System.out.println("[Coordinator]" + origem.host() + " :: " + origem.port() + " -- pediu um abort.");
                this.vereditos.addDecision(Decision.ABORT, id);
            }
            else
                System.out.println("[Coordinator] Late response!");
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
        CompletableFuture.supplyAsync(() -> {
            w.flush();
            return null;
        });
        this.broadcastMsg(msgType, pl);
    }


    public void startMS() {
        this.ms.start();
    }

    public void stopMS() {
        this.ms.stop();
        this.wP.close();
        this.wD.close();
    }
}