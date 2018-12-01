package ru.serbis.mnvp.rpc;

/**
 * Конфигуратор RPC вызова
 */
public class RpcCallConfig {
    /** Семантическая формула RCP процедуры вида funcName(t,t,t):t, где t моожет
     * принимать значения :
     *      I - целое число (4 байта)
     *      L - целое число (8 байт)
     *      F - дробное число (одинарной точности)
     *      D - дробное число (двойной точности)
     *      S - литерал
     *      V - void (не может быть типов аргумента, исползуется для
     *          возрата void из процедуры)
     * Данная формула используется при поиске и проверке вызовов входящего RPC
     * вызова
     **/
    private String semantic;
    /** Реализация обратного вызова, который выступает в качестве внешнего
     * обработчика rpc вызова */
    private RpcCallback rpcCallback;
    /** Флаг подтверждаемого RPС вызова. Если данный флаг установленн, то при
     * получении RPC запроса, узел до передачи управляения в процедуру выполнит
     * отправку RPC_ACCEPT пакета, означающего что запрос был принят и следует
     * ожидать подтверждения в течении заданного таймаута (указан в теле
     * сообщения) */
    private boolean confirmed;

    public RpcCallConfig(String semantic, RpcCallback rpcCallback) {
        this.semantic = semantic;
        this.rpcCallback = rpcCallback;
    }

    public RpcCallConfig() {
    }

    public String getSemantic() {
        return semantic;
    }

    public void setSemantic(String semantic) {
        this.semantic = semantic;
    }

    public RpcCallback getRpcCallback() {
        return rpcCallback;
    }

    public void setRpcCallback(RpcCallback rpcCallback) {
        this.rpcCallback = rpcCallback;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
