package ru.serbis.mnvp.rpc;

import java.util.List;

/**
 * Конфигурация системы RPC вызовов узла
 */
public class RpcConfig {
    /** Список конфигураций RPC процедур */
    private List<RpcCallConfig> rpcCallConfigList;

    public List<RpcCallConfig> getRpcCallConfigList() {
        return rpcCallConfigList;
    }

    public void setRpcCallConfigList(List<RpcCallConfig> rpcCallConfigList) {
        this.rpcCallConfigList = rpcCallConfigList;
    }
}
