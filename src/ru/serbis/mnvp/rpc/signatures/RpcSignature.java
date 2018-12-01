package ru.serbis.mnvp.rpc.signatures;

import java.util.ArrayList;
import java.util.List;

/**
 * Определение сигнатуры RPC вызова. Данный объект используется в двух
 * ситуациях:
 *      1. При регистрации вызова, происходит создание данного объекта для
 *         описания сигнатуры.
 *      2. При анализе входящего RPC вызова для анализа входщей сигнатуры и
 *         сравнении ее с зергистрированной в RPC объекте
 */
public class RpcSignature {
    /** Имя процедуры */
    private String procName;
    /** Список аргументов процедуры */
    private List<RpcArg> argsList = new ArrayList<>();
    /** Тип возвращаемого значения */
    private RpcType returnType;

    public String getProcName() {
        return procName;
    }

    public void setProcName(String procName) {
        this.procName = procName;
    }

    public List<RpcArg> getArgsList() {
        return argsList;
    }

    public void setArgsList(List<RpcArg> argsList) {
        this.argsList = argsList;
    }

    public RpcType getReturnType() {
        return returnType;
    }

    public void setReturnType(RpcType returnType) {
        this.returnType = returnType;
    }
}
