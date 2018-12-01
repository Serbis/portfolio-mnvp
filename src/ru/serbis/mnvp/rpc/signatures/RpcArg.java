package ru.serbis.mnvp.rpc.signatures;

/**
 * Описывает аргумент RPC процедуры
 */
public class RpcArg {
    /** Тип аргумента **/
    private RpcType rpcType;
    /** Значение аргумента */
    private Object value;

    public RpcArg() {
    }

    public RpcArg(RpcType rpcType) {
        this.rpcType = rpcType;
    }

    public RpcArg(RpcType rpcType, Object value) {
        this.rpcType = rpcType;
        this.value = value;
    }

    public RpcType getRpcType() {
        return rpcType;
    }

    public void setRpcType(RpcType rpcType) {
        this.rpcType = rpcType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
