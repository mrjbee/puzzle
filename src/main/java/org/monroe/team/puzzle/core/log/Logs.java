package org.monroe.team.puzzle.core.log;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicLong;

final public class Logs {

    private static AtomicLong transactionGenerator = new AtomicLong(0);
    public static Log bus = new Log(LoggerFactory.getLogger("puzzle.Bus"));

    private Logs() {}

    public static void resetTransactionId(final String tag){
        setTransactionId(tag+"-"+transactionGenerator.getAndAdd(1));
    }

    public static String getTransactionId(){
       return MDC.get("transactionId");
    }

    public static void setTransactionId(String transactionId){
        if (transactionId == null){
            transactionId = "fb-"+transactionGenerator.getAndAdd(1);
        }
        MDC.put("transactionId", transactionId);
    }
}
