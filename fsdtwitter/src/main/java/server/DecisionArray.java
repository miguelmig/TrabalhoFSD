package server;

import java.util.Arrays;

public class DecisionArray {

    Decision[] decisoes;

    DecisionArray(int tam) {
        this.decisoes = new Decision[tam];
        this.zeroOut();
    }

    public synchronized void addDecision(Decision d, int id) {
        this.decisoes[id] = d;
        notify();
    }

    private Decision precedence(Decision a, Decision b) {

        if(a == Decision.NOT_ANSWERED)
            return a;

        else if(a == Decision.COMMIT)
            return b;

        else {
            if(b == Decision.NOT_ANSWERED)
                return b;

            else
                return a;
        }

    }

    public synchronized void zeroOut() {
        Arrays.fill(this.decisoes, Decision.NOT_ANSWERED);
    }

    public synchronized Decision haveDecision() {
        Decision res = Decision.COMMIT;

        for(int i = 0; i < decisoes.length; i++) {
            res = precedence(res, decisoes[i]);
        }

        return res;
    }


}
