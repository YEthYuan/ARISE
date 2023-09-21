package org.aliyun.serverless.units;

import javax.annotation.security.RunAs;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Comparator;
import java.util.concurrent.Callable;


public class State {
    public enum CheckMethod {
        MORE,
        MORE_EQUAL,
        LESS,
        LESS_EQUAL
    }

    private static boolean compare(double val1, double val2, CheckMethod checkMethod) {
        switch (checkMethod) {
            case MORE:
                return val1 > val2;
            case MORE_EQUAL:
                return val1 >= val2;
            case LESS:
                return val1 < val2;
            case LESS_EQUAL:
                return val1 <= val2;
            default:
                return false;
        }
    }


    public static class Transfer {
        private final State nextState;
        private final CheckMethod checkMethod;
        private double threshold;
        private final CallbackFunction<?, ?> transferCallbackFunction;

        public Transfer(State nextState, CheckMethod checkMethod, double threshold) {
            this(nextState, checkMethod, threshold, null);
        }

        public Transfer(State nextState, CheckMethod checkMethod, double threshold, CallbackFunction<?, ?> transferCallbackFunction) {
            this.nextState = nextState;
            this.checkMethod = checkMethod;
            this.threshold = threshold;
            this.transferCallbackFunction = transferCallbackFunction;
        }

        public boolean check(double tarValue) {
            return compare(tarValue, threshold, checkMethod);
        }

        public State getNextState() {
            return nextState;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }
    }

    protected LinkedList<Transfer> transferList;
    protected CallbackFunction<?, ?> enterCallbackFunction;
    protected CallbackFunction<?, ?> exitCallbackFunction;

    public State() {
        this(null, null);
    }

    public State(CallbackFunction<?, ?> enterCallbackFunction, CallbackFunction<?, ?> exitCallbackFunction) {
        this.enterCallbackFunction = enterCallbackFunction;
        this.exitCallbackFunction = exitCallbackFunction;
        this.transferList = new LinkedList<Transfer>();
    }

    public void addTransfer(Transfer transfer) {
        this.transferList.add(transfer);
    }

    public void setThreshold(double threshold, int index) {
        this.transferList.get(index).setThreshold(threshold);
    }

    public State transfer(double tarValue) throws Exception {
        for (Transfer transfer : transferList) {
            if (transfer.check(tarValue)) {
                State nextState = transfer.getNextState();
                if (transfer.transferCallbackFunction != null) {
                    transfer.transferCallbackFunction.call();
                }
                if (nextState.enterCallbackFunction != null) {
                    nextState.enterCallbackFunction.call();
                }
                if (this.exitCallbackFunction != null) {
                    this.exitCallbackFunction.call();
                }
                return nextState;
            }
        }
        return this;
    }
}
