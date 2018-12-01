package ru.serbis.mnvp.rpc;

/**
 * Описыват исходящий RPC вызов
 */
public class RpcCallDef {
    /** Строка вызова */
    private String call = "_";
    /** Адрес целевого узла */
    private int destination = 1000000;
    /** Таймаут ожидания ответа (работает только для неподтверждаемых вызовов) */
    private int timeout = 5000;

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
